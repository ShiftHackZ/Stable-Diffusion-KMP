package com.shifthackz.aisdv1.domain.entity

/**
 * Validates a complete API prefix without changing its nested path.
 * LAN names, localhost, IPv4 and bracketed IPv6 hosts are supported.
 *
 * @param value HTTP(S) prefix without credentials, query or fragment.
 * @return Whether the endpoint can safely receive provider credentials.
 * @author Dmitriy Moroz
 */
fun isOpenAiCompatibleEndpoint(value: String): Boolean {
    if (value.isBlank() || value.any { it.isWhitespace() || it == '\\' || it.isISOControl() }) return false
    if ('?' in value || '#' in value) return false
    val match = Regex("^https?://([^/]+)(/.*)?$", RegexOption.IGNORE_CASE).matchEntire(value) ?: return false
    val authority = match.groupValues[1]
    if ('@' in authority) return false
    val host: String
    val port: String
    if (authority.startsWith("[")) {
        val closing = authority.indexOf(']')
        if (closing < 2) return false
        host = authority.substring(1, closing)
        if (':' !in host || !host.all { it in "0123456789abcdefABCDEF:." }) return false
        val suffix = authority.substring(closing + 1)
        if (suffix.isNotEmpty() && !suffix.startsWith(":")) return false
        port = suffix.removePrefix(":")
        if (suffix == ":") return false
    } else {
        if (authority.count { it == ':' } > 1) return false
        host = authority.substringBefore(':')
        if (host.isBlank() || !host.all { it.isLetterOrDigit() || it == '.' || it == '-' } || host.any { it.code > 127 }) return false
        port = authority.substringAfter(':', "")
        if (authority.endsWith(":")) return false
    }
    return port.isEmpty() || (port.all(Char::isDigit) && port.toIntOrNull() in 1..65535)
}
