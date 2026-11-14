package com.shifthackz.aisdv1.data.image

import android.graphics.BitmapFactory

/**
 * Decodes the actual bitmap, rejects corrupt content and releases it immediately.
 *
 * @param bytes Encoded image returned by the server.
 * @author Dmitriy Moroz
 */
internal actual fun decodedImageDimensions(bytes: ByteArray): Pair<Int, Int> {
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: error("Invalid image data")
    return try {
        check(bitmap.width > 0 && bitmap.height > 0) { "Invalid image dimensions" }
        bitmap.width to bitmap.height
    } finally {
        bitmap.recycle()
    }
}
