package com.shifthackz.aisdv1.domain.entity

/**
 * Waiting limits for network image generation requests, persisted as seconds rather than ordinals.
 *
 * @param seconds Request and socket inactivity limit in seconds; zero means no limit.
 * @author Dmitriy Moroz
 */
enum class GenerationRequestTimeout(val seconds: Int) {
    TWO_MINUTES(120),
    FIVE_MINUTES(300),
    TEN_MINUTES(600),
    THIRTY_MINUTES(1800),
    NO_LIMIT(0);

    companion object {
        /**
         * Restores a stored limit, keeping the original two-minute default for missing/unknown values.
         *
         * @param seconds Persisted timeout in seconds.
         * @author Dmitriy Moroz
         */
        fun fromSeconds(seconds: Int): GenerationRequestTimeout =
            entries.firstOrNull { it.seconds == seconds } ?: TWO_MINUTES
    }
}
