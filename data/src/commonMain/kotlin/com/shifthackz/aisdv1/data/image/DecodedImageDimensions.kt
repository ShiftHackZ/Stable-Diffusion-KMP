package com.shifthackz.aisdv1.data.image

/**
 * Fully decodes image bytes before they can enter the gallery.
 *
 * @param bytes Encoded image returned by the server.
 * @return Actual positive pixel width and height; throws for undecodable content.
 * @author Dmitriy Moroz
 */
internal expect fun decodedImageDimensions(bytes: ByteArray): Pair<Int, Int>
