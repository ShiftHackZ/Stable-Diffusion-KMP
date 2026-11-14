package com.shifthackz.aisdv1.presentation.screen.setup.validation

import com.shifthackz.aisdv1.core.validation.common.CommonStringValidator
import com.shifthackz.aisdv1.core.validation.path.FilePathValidator
import com.shifthackz.aisdv1.core.validation.url.UrlValidator
import com.shifthackz.aisdv1.domain.entity.isOpenAiCompatibleEndpoint
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupState
import com.shifthackz.aisdv1.presentation.screen.setup.viewmodel.mapFilePathToValidationError
import com.shifthackz.aisdv1.presentation.screen.setup.viewmodel.mapStringToValidationError
import com.shifthackz.aisdv1.presentation.screen.setup.viewmodel.mapUrlToValidationError

/**
 * Validation output for the setup form.
 *
 * `state` may contain newly assigned validation errors or a modal requesting localhost
 * confirmation, so callers must use it even when `isValid` is false.
 */
internal data class ServerSetupValidationResult(
    val isValid: Boolean,
    val state: ServerSetupState,
)

/**
 * Validates the currently selected provider form.
 *
 * Remote providers validate URL/auth/API key inputs. Local providers validate that either a
 * downloaded model is selected or, when custom models are enabled, that the custom path is usable.
 */
internal fun ServerSetupState.validateServerSetup(
    urlValidator: UrlValidator,
    stringValidator: CommonStringValidator,
    filePathValidator: FilePathValidator,
): ServerSetupValidationResult = when (mode) {
    ServerSource.AUTOMATIC1111 -> {
        if (demoMode) ServerSetupValidationResult(true, this)
        else validateServerUrlAndCredentials(serverUrl, urlValidator, stringValidator)
    }

    ServerSource.SWARM_UI -> validateServerUrlAndCredentials(swarmUiUrl, urlValidator, stringValidator)
    ServerSource.HORDE -> validateApiKey(
        key = hordeApiKey,
        stringValidator = stringValidator,
        useDefault = hordeDefaultApiKey,
        update = { error -> copy(hordeApiKeyValidationError = error) },
    )

    ServerSource.HUGGING_FACE -> validateApiKey(
        key = huggingFaceApiKey,
        stringValidator = stringValidator,
        update = { error -> copy(huggingFaceApiKeyValidationError = error) },
    )

    ServerSource.OPEN_AI_COMPATIBLE -> {
        val valid = isOpenAiCompatibleEndpoint(openAiCompatibleUrl.trim())
        ServerSetupValidationResult(valid, copy(
            openAiCompatibleUrlValidationError = if (valid) null else ServerSetupState.ValidationError.InvalidUrl,
        ))
    }

    ServerSource.OPEN_AI -> validateApiKey(
        key = openAiApiKey,
        stringValidator = stringValidator,
        update = { error -> copy(openAiApiKeyValidationError = error) },
    )

    ServerSource.STABILITY_AI -> validateApiKey(
        key = stabilityAiApiKey,
        stringValidator = stringValidator,
        update = { error -> copy(stabilityAiApiKeyValidationError = error) },
    )

    ServerSource.FAL_AI -> validateApiKey(
        key = falAiApiKey,
        stringValidator = stringValidator,
        update = { error -> copy(falAiApiKeyValidationError = error) },
    )

    ServerSource.ARLI_AI -> validateApiKey(
        key = arliAiApiKey,
        stringValidator = stringValidator,
        update = { error -> copy(arliAiApiKeyValidationError = error) },
    )

    ServerSource.LOCAL_MICROSOFT_ONNX -> validateLocalModel(
        customModel = localOnnxCustomModel,
        customModelPath = localOnnxCustomModelPath,
        hasDownloadedSelection = localOnnxModels.any { it.selected && it.downloaded },
        filePathValidator = filePathValidator,
        update = { error -> copy(localCustomOnnxPathValidationError = error) },
    )

    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> validateLocalModel(
        customModel = localMediaPipeCustomModel,
        customModelPath = localMediaPipeCustomModelPath,
        hasDownloadedSelection = localMediaPipeModels.any { it.selected && it.downloaded },
        filePathValidator = filePathValidator,
        update = { error -> copy(localCustomMediaPipePathValidationError = error) },
    )

    ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> validateLocalModel(
        customModel = localSdxlCustomModel,
        customModelPath = localSdxlCustomModelPath,
        hasDownloadedSelection = localSdxlModels.any { it.selected && it.downloaded },
        filePathValidator = filePathValidator,
        update = { error -> copy(localCustomSdxlPathValidationError = error) },
    )

    ServerSource.LOCAL_APPLE_CORE_ML -> ServerSetupValidationResult(
        isValid = localCoreMlModels.any { it.selected && it.downloaded },
        state = this,
    )

    ServerSource.LOCAL_APPLE_BONSAI -> ServerSetupValidationResult(
        isValid = localBonsaiModels.any { it.selected && it.downloaded },
        state = this,
    )
}

private fun ServerSetupState.validateApiKey(
    key: String,
    stringValidator: CommonStringValidator,
    useDefault: Boolean = false,
    update: ServerSetupState.(ServerSetupState.ValidationError?) -> ServerSetupState,
): ServerSetupValidationResult {
    if (useDefault) return ServerSetupValidationResult(true, this)
    val validation = stringValidator(key)
    val error = validation.mapStringToValidationError()
    return ServerSetupValidationResult(validation.isValid, update(error))
}

private fun ServerSetupState.validateLocalModel(
    customModel: Boolean,
    customModelPath: String,
    hasDownloadedSelection: Boolean,
    filePathValidator: FilePathValidator,
    update: ServerSetupState.(ServerSetupState.ValidationError?) -> ServerSetupState,
): ServerSetupValidationResult {
    if (!customModel) return ServerSetupValidationResult(hasDownloadedSelection, this)
    val validation = filePathValidator(customModelPath)
    val error = validation.mapFilePathToValidationError()
    return ServerSetupValidationResult(validation.isValid, update(error))
}

private fun ServerSetupState.validateServerUrlAndCredentials(
    url: String,
    urlValidator: UrlValidator,
    stringValidator: CommonStringValidator,
): ServerSetupValidationResult {
    val urlValidation = urlValidator(url)
    var isValid = urlValidation.isValid
    val urlError = urlValidation.mapUrlToValidationError()
    var newState = copy(
        serverUrlValidationError = if (mode == ServerSource.AUTOMATIC1111) {
            urlError
        } else {
            serverUrlValidationError
        },
        swarmUiUrlValidationError = if (mode == ServerSource.SWARM_UI) {
            urlError
        } else {
            swarmUiUrlValidationError
        },
    )
    if (authType == ServerSetupState.AuthType.HTTP_BASIC) {
        val loginValidation = stringValidator(login)
        val passwordValidation = stringValidator(password)
        val loginError = loginValidation.mapStringToValidationError()
        val passwordError = passwordValidation.mapStringToValidationError()
        newState = newState.copy(
            loginValidationError = loginError,
            passwordValidationError = passwordError,
        )
        isValid = isValid && loginValidation.isValid && passwordValidation.isValid
    }
    if (urlValidation.validationError is UrlValidator.Error.Localhost
        && newState.loginValidationError == null
        && newState.passwordValidationError == null
    ) {
        newState = newState.copy(modal = ServerSetupState.Modal.ConnectLocalHost)
    }
    return ServerSetupValidationResult(isValid, newState)
}
