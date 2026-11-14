package com.shifthackz.aisdv1.feature.openaioauth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Device authorization request adapted from OpenAI Codex device_code_auth.rs (Apache-2.0).
 * @param clientId Fixed public OAuth application identifier.
 * @author Dmitriy Moroz
 */
@Serializable
internal class DeviceCodeRequest(@SerialName("client_id") val clientId: String)
