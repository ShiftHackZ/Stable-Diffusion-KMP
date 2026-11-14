package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.data.image.decodedImageDimensions
import com.shifthackz.aisdv1.data.mappers.toOpenAiCompatibleResult
import com.shifthackz.aisdv1.data.mappers.toOpenAiCompatibleRequest
import com.shifthackz.aisdv1.domain.datasource.OpenAiCompatibleGenerationDataSource
import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleConnection
import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleHttpFailure
import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleImageFailure
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.network.api.openaicompatible.OpenAiCompatibleApi
import com.shifthackz.aisdv1.network.api.openaicompatible.OpenAiCompatibleHttpException
import kotlinx.coroutines.CancellationException
import kotlin.io.encoding.Base64
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Resolves and decodes the first image before constructing a gallery candidate.
 *
 * @param api Independent compatible-server transport.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalTime::class)
class KtorOpenAiCompatibleGenerationRemoteDataSource(
    private val api: OpenAiCompatibleApi,
) : OpenAiCompatibleGenerationDataSource.Remote {
    /**
     * Preserves the distinction between reachable and unsupported model discovery.
     *
     * @param url Complete API prefix.
     * @param apiKey Optional bearer token.
     * @author Dmitriy Moroz
     */
    override suspend fun probe(url: String, apiKey: String): OpenAiCompatibleConnection =
        compatibleCall {
            if (api.probe(url, apiKey)) OpenAiCompatibleConnection.REACHABLE else OpenAiCompatibleConnection.UNVERIFIED
        }

    /**
     * Resolves the first image, rejects invalid content and records actual pixel dimensions.
     *
     * @param url Complete API prefix.
     * @param apiKey Optional token used only for API requests.
     * @param payload Image description, arbitrary model ID and requested dimensions.
     * @author Dmitriy Moroz
     */
    override suspend fun textToImage(
        url: String,
        apiKey: String,
        payload: TextToImagePayload,
    ) =
        imageCall {
            api.generate(url, apiKey, payload.toOpenAiCompatibleRequest()).data.firstOrNull()?.let { image ->
                val base64 = image.base64
                val imageUrl = image.url
                val bytes = when {
                    !base64.isNullOrBlank() -> Base64.Default.decode(base64.filterNot(Char::isWhitespace))
                    !imageUrl.isNullOrBlank() -> api.downloadImage(imageUrl)
                    else -> error("The server returned no image data")
                }
                val (width, height) = decodedImageDimensions(bytes)
                payload.toOpenAiCompatibleResult(
                    image = Base64.Default.encode(bytes),
                    width = width,
                    height = height,
                    createdAtMillis = Clock.System.now().toEpochMilliseconds(),
                )
            } ?: error("The server returned no images")
        }

    /**
     * Discards image URLs and response payloads from transport, JSON and decoder exceptions.
     * Cancellation still propagates; safe status failures retain their localized category.
     *
     * @param block Image generation, resolution and decoding operation.
     * @author Dmitriy Moroz
     */
    private suspend fun <T> imageCall(block: suspend () -> T): T = try {
        compatibleCall(block)
    } catch (error: CancellationException) {
        throw error
    } catch (error: OpenAiCompatibleHttpFailure) {
        throw error
    } catch (_: Exception) {
        throw OpenAiCompatibleImageFailure()
    }

    /**
     * Maps safe transport status while preserving cancellation and other failures.
     *
     * @param block Provider operation to execute.
     * @author Dmitriy Moroz
     */
    private suspend fun <T> compatibleCall(block: suspend () -> T): T = try {
        block()
    } catch (error: OpenAiCompatibleHttpException) {
        throw OpenAiCompatibleHttpFailure(error.statusCode)
    }
}
