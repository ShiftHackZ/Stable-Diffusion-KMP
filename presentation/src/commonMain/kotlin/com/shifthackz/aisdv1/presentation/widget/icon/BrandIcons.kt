package com.shifthackz.aisdv1.presentation.widget.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Provides reusable vector artwork for community, platform, and developer links.
 *
 * @author Dmitriy Moroz
 */
object BrandIcons {

    /**
     * Exposes the `Telegram` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val Telegram: ImageVector
        get() = telegram ?: brandIcon(
            name = "Brand.Telegram",
            pathData = TELEGRAM_PATH,
        ).also { telegram = it }

    /**
     * Exposes the `Discord` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val Discord: ImageVector
        get() = discord ?: brandIcon(
            name = "Brand.Discord",
            pathData = DISCORD_PATH,
        ).also { discord = it }

    /**
     * Exposes the `Apple` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val Apple: ImageVector
        get() = apple ?: brandIcon(
            name = "Brand.Apple",
            pathData = APPLE_PATH,
        ).also { apple = it }

    /**
     * Exposes the `Moroz` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val Moroz: ImageVector
        get() = moroz ?: brandIcon(
            name = "Brand.Moroz",
            pathData = MOROZ_PATH,
            viewportWidth = 100f,
            viewportHeight = 100f,
            pathFillType = PathFillType.NonZero,
            strokeLineWidth = 4.6f,
        ).also { moroz = it }
}

/**
 * Executes the `brandIcon` step in the SDAI presentation layer.
 *
 * @param name name value consumed by the API.
 * @param pathData path data value consumed by the API.
 * @param viewportWidth viewport width value consumed by the API.
 * @param viewportHeight viewport height value consumed by the API.
 * @param pathFillType path fill type value consumed by the API.
 * @param strokeLineWidth stroke line width value consumed by the API.
 * @param scaleX scale x value consumed by the API.
 * @param scaleY scale y value consumed by the API.
 * @param pivotX pivot x value consumed by the API.
 * @param pivotY pivot y value consumed by the API.
 * @author Dmitriy Moroz
 */
private fun brandIcon(
    name: String,
    pathData: String,
    viewportWidth: Float = 24f,
    viewportHeight: Float = 24f,
    pathFillType: PathFillType = PathFillType.EvenOdd,
    strokeLineWidth: Float = 0f,
    scaleX: Float = 1f,
    scaleY: Float = 1f,
    pivotX: Float = 0f,
    pivotY: Float = 0f,
): ImageVector = brandIcon(
    name = name,
    pathData = listOf(pathData),
    viewportWidth = viewportWidth,
    viewportHeight = viewportHeight,
    pathFillType = pathFillType,
    strokeLineWidth = strokeLineWidth,
    scaleX = scaleX,
    scaleY = scaleY,
    pivotX = pivotX,
    pivotY = pivotY,
)

/**
 * Executes the `brandIcon` step in the SDAI presentation layer.
 *
 * @param name name value consumed by the API.
 * @param pathData path data value consumed by the API.
 * @param viewportWidth viewport width value consumed by the API.
 * @param viewportHeight viewport height value consumed by the API.
 * @param pathFillType path fill type value consumed by the API.
 * @param strokeLineWidth stroke line width value consumed by the API.
 * @param scaleX scale x value consumed by the API.
 * @param scaleY scale y value consumed by the API.
 * @param pivotX pivot x value consumed by the API.
 * @param pivotY pivot y value consumed by the API.
 * @author Dmitriy Moroz
 */
private fun brandIcon(
    name: String,
    pathData: List<String>,
    viewportWidth: Float = 24f,
    viewportHeight: Float = 24f,
    pathFillType: PathFillType = PathFillType.EvenOdd,
    strokeLineWidth: Float = 0f,
    scaleX: Float = 1f,
    scaleY: Float = 1f,
    pivotX: Float = 0f,
    pivotY: Float = 0f,
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = viewportWidth,
    viewportHeight = viewportHeight,
).apply {
    val transformed = scaleX != 1f || scaleY != 1f
    if (transformed) {
        addGroup(
            scaleX = scaleX,
            scaleY = scaleY,
            pivotX = pivotX,
            pivotY = pivotY,
        )
    }
    pathData.forEach { path ->
        addPath(
            pathData = PathParser().parsePathString(path).toNodes(),
            pathFillType = pathFillType,
            fill = SolidColor(Color.Black),
            stroke = strokeLineWidth
                .takeIf { it > 0f }
                ?.let { SolidColor(Color.Black) },
            strokeLineWidth = strokeLineWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
    if (transformed) {
        clearGroup()
    }
}.build()

/**
 * Exposes the `telegram` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private var telegram: ImageVector? = null
/**
 * Exposes the `discord` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private var discord: ImageVector? = null
/**
 * Exposes the `apple` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private var apple: ImageVector? = null
/**
 * Exposes the `moroz` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private var moroz: ImageVector? = null

/**
 * Exposes the `TELEGRAM_PATH` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private const val TELEGRAM_PATH =
    "M12 0C5.373 0 0 5.373 0 12s5.373 12 12 12 12-5.373 12-12S18.627 0 12 0zm5.562 8.162l-2.015 9.49c-.153.685-.554.851-1.122.53l-3.105-2.29-1.497 1.44c-.166.166-.305.305-.625.305l.223-3.155 5.738-5.187c.25-.222-.054-.345-.386-.123l-7.09 4.465-3.052-.953c-.663-.207-.675-.663.138-.981l11.93-4.599c.553-.207 1.036.123.863 1.058z"

/**
 * Exposes the `DISCORD_PATH` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private const val DISCORD_PATH =
    "M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057a.082.082 0 0 0 .031.057 19.9 19.9 0 0 0 5.993 3.03.078.078 0 0 0 .084-.028 14.09 14.09 0 0 0 1.226-1.994.076.076 0 0 0-.041-.106 13.107 13.107 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10.2 10.2 0 0 0 .372-.292.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.077.077 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 0 0-.031-.03zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418z"

/**
 * Exposes the `APPLE_PATH` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private const val APPLE_PATH =
    "M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.81-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z"

/**
 * Exposes the `MOROZ_PATH` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private const val MOROZ_PATH =
    "M75.25,86.64C75.81,86.64 76.55,86.53 76.66,86.25C76.72,86.02 76.44,85.74 75.81,85.68C72.53,83.76 74.23,75.71 76.95,66.71C78.76,60.76 81.02,54.42 82.67,49.09C83.74,45.64 84.54,42.63 84.71,40.43C84.88,36.97 84.03,35.55 82.50,35.55C76.04,35.55 57.69,61.72 53.16,68.58C56.21,59.74 63.63,43.82 67.60,30.85C70.77,20.38 71.68,11.88 66.13,10.75C55.37,12.50 42.34,29.95 32.37,43.03C31.41,44.50 30.50,45.58 30.44,45.58C30.73,43.20 31.41,37.37 29.99,37.37C29.76,37.37 29.59,37.48 29.42,37.65C29.25,38.22 29.31,41.50 29.42,41.73C29.76,41.84 28.97,46.71 28.52,48.64C22.12,58.32 16.23,68.35 13.51,77.19C13.17,78.26 12.94,79.22 12.88,79.73C13.05,81.09 13.45,81.72 14.07,81.72C15.60,81.72 18.32,77.70 20.70,72.77C24.21,65.86 27.38,56.79 29.82,48.75C37.47,37.99 54.63,13.86 65.62,11.99C70.09,13.07 69.24,20.43 66.35,29.89C62.28,43.26 52.48,65.80 51.17,69.99C50.95,70.79 50.89,71.12 51.00,71.29C51.12,71.58 51.34,71.69 51.57,71.69C52.14,71.69 52.93,71.07 53.89,69.77C57.29,64.50 76.38,36.97 81.87,36.97C82.72,36.97 83.29,37.65 83.35,39.24L83.46,39.18C83.97,39.18 82.16,45.47 80.00,52.49C78.13,58.55 76.04,65.12 74.96,68.75L74.17,71.35C72.24,81.15 71.39,86.64 75.25,86.64ZM14.41,80.07C14.07,80.07 14.19,79.17 14.64,77.75C16.11,72.94 21.10,62.18 24.55,57.13C26.14,54.53 26.82,53.40 26.93,53.40C27.10,53.40 26.82,54.13 26.42,55.49C24.21,62.69 19.00,74.30 15.94,78.60C15.21,79.62 14.70,80.07 14.41,80.07Z"
