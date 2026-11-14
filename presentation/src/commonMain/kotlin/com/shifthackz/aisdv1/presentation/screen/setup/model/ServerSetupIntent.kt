package com.shifthackz.aisdv1.presentation.screen.setup.model

import com.shifthackz.aisdv1.core.mvi.MviIntent
import com.shifthackz.aisdv1.domain.entity.FeatureTag
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness
import com.shifthackz.aisdv1.domain.entity.ServerSourceType

/**
 * User events handled by the provider setup wizard.
 *
 * Source-selection intents update search/filter/sort state, provider-form intents update the
 * active configuration form, and local-model intents drive download/delete confirmation flows.
 */
sealed interface ServerSetupIntent : MviIntent {
    /** Starts device authorization for the selected ChatGPT provider. @author Dmitriy Moroz */
    data object OAuthLogin : ServerSetupIntent
    /** Cancels pending polling without deleting the committed session. @author Dmitriy Moroz */
    data object OAuthCancel : ServerSetupIntent
    /** Deletes the local protected session, including an unreadable envelope. @author Dmitriy Moroz */
    data object OAuthLogout : ServerSetupIntent
    /** Opens the trusted verification page for the current transient device code. @author Dmitriy Moroz */
    data object OAuthOpenBrowser : ServerSetupIntent
    data class UpdateServerMode(val mode: ServerSource) : ServerSetupIntent
    data class UpdateSourceSearchQuery(val query: String) : ServerSetupIntent
    data class UpdateSourceTypeFilter(val type: ServerSourceType?) : ServerSetupIntent
    data class ToggleSourceReadinessFilter(val readiness: ServerSourceReadiness) : ServerSetupIntent
    data class ToggleSourceTagFilter(val tag: FeatureTag) : ServerSetupIntent
    data object ResetSourceFilters : ServerSetupIntent
    data class UpdateSourceSortOrder(val sortOrder: ServerSetupState.SourceSortOrder) : ServerSetupIntent
    data class UpdateServerUrl(val url: String) : ServerSetupIntent
    data class UpdateSwarmUiUrl(val url: String) : ServerSetupIntent
    data class UpdateAuthType(val type: ServerSetupState.AuthType) : ServerSetupIntent
    data class UpdateLogin(val login: String) : ServerSetupIntent
    data class UpdatePassword(val password: String) : ServerSetupIntent
    data class UpdatePasswordVisibility(val visible: Boolean) : ServerSetupIntent
    data class UpdateHordeApiKey(val key: String) : ServerSetupIntent
    data class UpdateOpenAiCompatibleUrl(val url: String) : ServerSetupIntent
    data class UpdateOpenAiCompatibleApiKey(val key: String) : ServerSetupIntent
    data object AcknowledgeUnverifiedConnection : ServerSetupIntent
    data class UpdateOpenAiApiKey(val key: String) : ServerSetupIntent
    data class UpdateStabilityAiApiKey(val key: String) : ServerSetupIntent
    data class UpdateFalAiApiKey(val key: String) : ServerSetupIntent
    data class UpdateArliAiApiKey(val key: String) : ServerSetupIntent
    data class UpdateHuggingFaceApiKey(val key: String) : ServerSetupIntent
    data class UpdateHuggingFaceModel(val model: String) : ServerSetupIntent
    data class UpdateDemoMode(val value: Boolean) : ServerSetupIntent
    data class UpdateHordeDefaultApiKey(val value: Boolean) : ServerSetupIntent
    data class SelectLocalModelPath(val value: String) : ServerSetupIntent
    data class SelectLocalModel(val model: ServerSetupState.LocalModel) : ServerSetupIntent
    data class AllowLocalCustomModel(val allow: Boolean) : ServerSetupIntent
    data class LaunchUrl(val link: ServerSetupLink) : ServerSetupIntent
    data object MainButtonClick : ServerSetupIntent
    data object DismissDialog : ServerSetupIntent
    data object ShowSourceFilters : ServerSetupIntent
    data object ShowSourceSort : ServerSetupIntent
    data object NavigateBack : ServerSetupIntent
    data object LaunchManageStoragePermission : ServerSetupIntent
    data object ConnectToLocalHost : ServerSetupIntent

    sealed interface LocalModel : ServerSetupIntent {
        data class ClickReduce(val model: ServerSetupState.LocalModel) : LocalModel
        data class DownloadConfirm(val modelId: String, val url: String) : LocalModel
        data class DeleteConfirm(val model: ServerSetupState.LocalModel) : LocalModel
    }
}

/**
 * External setup/help links routed through `LinksProvider`.
 */
enum class ServerSetupLink {
    A1111Instructions,
    SwarmUiInstructions,
    HordeInfo,
    HordeSignUp,
    HuggingFaceInfo,
    OpenAiInfo,
    StabilityAiInfo,
    FalAiInfo,
    ArliAiInfo,
}
