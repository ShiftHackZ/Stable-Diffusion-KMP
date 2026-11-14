@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class, UnsafeNumber::class)

package com.shifthackz.aisdv1.feature.openaioauth.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryCreate
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFRetain
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSLog
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * Device-only, unlocked Keychain envelope. Updates replace one item atomically without delete/add.
 * No iCloud synchronization or plaintext defaults fallback is used.
 * @author Dmitriy Moroz
 */
internal class SecureSessionStore : SessionStore {
    /**
     * Reads the UTF-8 envelope; absence is distinct from a Keychain or decoding failure.
     * @author Dmitriy Moroz
     */
    override fun read(): String? = withBaseQuery { base ->
        memScoped {
            val result = alloc<CFTypeRefVar>()
            result.value = null
            val query = base + mapOf(kSecReturnData to kCFBooleanTrue, kSecMatchLimit to kSecMatchLimitOne)
            val status = query.withDictionary("read.query") { dictionary ->
                perform("read") { SecItemCopyMatching(dictionary, result.ptr) }
            }
            if (status == errSecItemNotFound) return@memScoped null
            check(status == errSecSuccess) { "Protected session read failed" }
            val value = result.value ?: error("Protected session is empty")
            try {
                prepare("read.result") {
                    val data = CFBridgingRelease(CFRetain(value)) as NSData
                    NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
                        ?: error("Invalid protected session")
                }
            } finally { CFRelease(value) }
        }
    }

    /**
     * Atomically updates one protected item, creates it when absent, or deletes it without decoding.
     * @param value Complete UTF-8 session envelope, or null for explicit local deletion.
     * @author Dmitriy Moroz
     */
    override fun replace(value: String?) {
        if (value == null) {
            val status = withBaseQuery { base ->
                base.withDictionary("delete.query") { dictionary ->
                    perform("delete") { SecItemDelete(dictionary) }
                }
            }
            check(status == errSecSuccess || status == errSecItemNotFound) { "Protected session deletion failed" }
            return
        }
        val data = prepare("replace.encoding") {
            (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: error("Invalid session encoding")
        }
        withRetained(data, "replace.data") { cfData ->
            withBaseQuery { base ->
                val attributes = mapOf(kSecValueData to cfData, kSecAttrAccessible to kSecAttrAccessibleWhenUnlockedThisDeviceOnly)
                val status = base.withDictionary("update.query") { query ->
                    attributes.withDictionary("update.attributes") { changes ->
                        perform("update") { SecItemUpdate(query, changes) }
                    }
                }
                if (status == errSecItemNotFound) {
                    val addStatus = (base + attributes).withDictionary("add.query") { dictionary ->
                        perform("add") { SecItemAdd(dictionary, null) }
                    }
                    check(addStatus == errSecSuccess) { "Protected session creation failed" }
                } else check(status == errSecSuccess) { "Protected session replacement failed" }
            }
        }
    }

    /**
     * Keeps actual Core Foundation service/account references alive throughout one storage action.
     * @param block Action using the fixed generic-password query; references must not escape.
     * @author Dmitriy Moroz
     */
    private fun <T> withBaseQuery(block: (Map<CFStringRef?, CFTypeRef?>) -> T): T =
        withRetained("${NSBundle.mainBundle.bundleIdentifier ?: "SDAI"}.openai-oauth", "query.service") { service ->
            withRetained("session", "query.account") { account ->
                block(mapOf(kSecClass to kSecClassGenericPassword, kSecAttrService to service, kSecAttrAccount to account))
            }
        }

    /**
     * Builds a CF dictionary from native pointers, without boxing them through an Objective-C map.
     * Values are retained by the enclosing scopes, so dictionary callbacks do not take ownership.
     * @param stage Fixed non-sensitive preparation label used only when construction fails.
     * @param block Synchronous Keychain action; dictionary references must not escape.
     * @author Dmitriy Moroz
     */
    private fun <T> Map<CFStringRef?, CFTypeRef?>.withDictionary(stage: String, block: (CFDictionaryRef) -> T): T = memScoped {
        val dictionary = prepare(stage) {
            val keys = allocArrayOf(*this@withDictionary.keys.toTypedArray())
            val values = allocArrayOf(*this@withDictionary.values.toTypedArray())
            CFDictionaryCreate(kCFAllocatorDefault, keys.reinterpret(), values.reinterpret(), this@withDictionary.size.convert(), null, null)
                ?: error("Protected session dictionary unavailable")
        }
        try { block(dictionary) } finally { CFRelease(dictionary) }
    }

    /**
     * Bridges one Foundation value and releases its owned CF reference after the storage action.
     * @param value Foundation-compatible value; never included in diagnostics.
     * @param stage Fixed non-sensitive preparation label.
     * @param block Synchronous action; the retained reference must not escape.
     * @author Dmitriy Moroz
     */
    private fun <T> withRetained(value: Any, stage: String, block: (CFTypeRef) -> T): T {
        val retained = prepare(stage) { CFBridgingRetain(value) ?: error("Protected session bridge unavailable") }
        return try { block(retained) } finally { CFRelease(retained) }
    }

    /**
     * Records only a fixed operation label and numeric Keychain status, without query or data.
     * @param operation Fixed label identifying the Keychain function.
     * @param block Keychain operation returning its OSStatus.
     * @author Dmitriy Moroz
     */
    private fun perform(operation: String, block: () -> Int): Int = block().also { status ->
        NSLog("OpenAI OAuth protected storage operation=$operation status=$status")
    }

    /**
     * Reports preparation failures by fixed stage and exception class, never exception messages.
     * @param stage Fixed non-sensitive preparation label.
     * @param block Bridge, encoding or dictionary construction action.
     * @author Dmitriy Moroz
     */
    private fun <T> prepare(stage: String, block: () -> T): T = try { block() } catch (error: Exception) {
        NSLog("OpenAI OAuth protected storage stage=$stage error=${error::class.simpleName ?: "Exception"}")
        throw error
    }
}
