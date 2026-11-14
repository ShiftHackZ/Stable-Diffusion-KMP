package com.shifthackz.aisdv1.feature.openaioauth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Identifies the exact pending authorization to poll.
 * @param deviceAuthId Identifier from the initial challenge.
 * @param userCode Code from the same challenge.
 * @author Dmitriy Moroz
 */
@Serializable
internal class DevicePollRequest(
    @SerialName("device_auth_id") val deviceAuthId: String,
    @SerialName("user_code") val userCode: String,
)
