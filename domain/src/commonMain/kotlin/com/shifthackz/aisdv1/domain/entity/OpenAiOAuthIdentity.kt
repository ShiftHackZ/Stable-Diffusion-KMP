package com.shifthackz.aisdv1.domain.entity

/**
 * Token-free account identity captured by submitted generation work and unsaved results.
 * A new login changes [revision]; ordinary token refresh and process restart preserve it.
 * @param accountId ChatGPT account identifier received from the completed authorization grant.
 * @param revision Random revision of the securely persisted login session.
 * @author Dmitriy Moroz
 */
data class OpenAiOAuthIdentity(
    val accountId: String,
    val revision: String,
)
