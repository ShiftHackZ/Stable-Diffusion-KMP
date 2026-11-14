package com.shifthackz.aisdv1.feature.openaioauth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Pending device authorization retained only for the current login attempt.
 * @param deviceAuthId Opaque challenge identifier used during polling.
 * @param userCode Current wire spelling of the user-facing one-time code.
 * @param usercode Legacy wire spelling accepted by the same protocol.
 * @param interval Server polling interval as whole seconds or a decimal string.
 * @author Dmitriy Moroz
 */
@Serializable
internal class DeviceCodeResponse(
    @SerialName("device_auth_id") val deviceAuthId: String,
    @SerialName("user_code") val userCode: String? = null,
    val usercode: String? = null,
    val interval: JsonElement? = null,
)
