package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload

/** Direct ChatGPT image generation using a session captured at work submission.
 * @author Dmitriy Moroz
 */
interface OpenAiOAuthGenerationRepository {
    /**
     * Validates the pinned session and saves a single decoded image through the shared gallery.
     * @param payload Image description/dimensions and token-free source/account/revision snapshot.
     * @author Dmitriy Moroz
     */
    suspend fun generateFromText(payload: TextToImagePayload): AiGenerationResult
}
