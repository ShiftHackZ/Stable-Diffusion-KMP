package com.shifthackz.aisdv1.feature.openaioauth.model

/**
 * Token-free account identity captured by generation work.
 * Login replacement changes [revision]; ordinary token rotation and process restart preserve it.
 * @param accountId Backend account identifier, without credentials.
 * @param revision Random session identity used to reject work from an earlier login.
 * @author Dmitriy Moroz
 */
data class OAuthIdentity(
    val accountId: String,
    val revision: String,
)
