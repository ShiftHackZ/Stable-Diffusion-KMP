package com.shifthackz.aisdv1.presentation.screen.setup.model

/**
 * Mutually exclusive device-login phases used by both configuration content and pinned actions.
 * A pending replacement login hides the previous account's actions until it finishes or cancels.
 * @author Dmitriy Moroz
 */
internal enum class OpenAiOAuthSetupPhase {
    /** No usable local session or pending login. */
    SIGNED_OUT,

    /** The device-code request is running and can be cancelled. */
    REQUESTING_CODE,

    /** A complete device code is ready for browser authorization. */
    AWAITING_AUTHORIZATION,

    /** Protected storage contains a usable local account. */
    SIGNED_IN,

    /** Protected storage requires recovery before the account can be used. */
    STORAGE_ERROR,
}

/**
 * Resolves one configuration phase, giving an active login precedence over prior session state.
 * Storage failures take precedence over account labels to avoid implying that connection is ready.
 * @author Dmitriy Moroz
 */
internal val ServerSetupState.openAiOAuthPhase: OpenAiOAuthSetupPhase
    get() = when {
        openAiOAuthBusy && !openAiOAuthCode.isNullOrBlank() -> OpenAiOAuthSetupPhase.AWAITING_AUTHORIZATION
        openAiOAuthBusy -> OpenAiOAuthSetupPhase.REQUESTING_CODE
        openAiOAuthStorageError -> OpenAiOAuthSetupPhase.STORAGE_ERROR
        openAiOAuthAccount != null -> OpenAiOAuthSetupPhase.SIGNED_IN
        else -> OpenAiOAuthSetupPhase.SIGNED_OUT
    }
