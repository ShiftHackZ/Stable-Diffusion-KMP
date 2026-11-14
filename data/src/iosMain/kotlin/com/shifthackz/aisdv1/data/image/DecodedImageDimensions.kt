@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.shifthackz.aisdv1.data.image

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage

/**
 * Uses the platform image decoder and reports pixel dimensions rather than UIKit points.
 *
 * @param bytes Encoded image returned by the server.
 * @author Dmitriy Moroz
 */
internal actual fun decodedImageDimensions(bytes: ByteArray): Pair<Int, Int> {
    check(bytes.isNotEmpty()) { "Empty image data" }
    val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
    val image = UIImage.imageWithData(data)?.CGImage ?: error("Invalid image data")
    val width = CGImageGetWidth(image).toInt()
    val height = CGImageGetHeight(image).toInt()
    check(width > 0 && height > 0) { "Invalid image dimensions" }
    return width to height
}
