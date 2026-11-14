@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.screen.img2img

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArtTrack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.model.compactDisplayName
import com.shifthackz.aisdv1.presentation.widget.scrollbar.verticalScrollbar


/**
 * Renders the `UnsupportedImageToImageBody` UI for the SDAI presentation layer.
 *
 * @param modifier Compose modifier applied to the rendered UI.
 * @param mode mode value consumed by the API.
 * @param strings strings value consumed by the API.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UnsupportedImageToImageBody(
    modifier: Modifier = Modifier,
    mode: ServerSource,
    strings: ImageToImageStrings,
) {
    Column(
        modifier = modifier.padding(horizontal = 36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier.size(96.dp),
            imageVector = Icons.Default.DeviceUnknown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            modifier = Modifier.padding(top = 20.dp),
            text = strings.unsupportedTitle,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier.padding(top = 14.dp),
            text = when (mode) {
                ServerSource.OPEN_AI -> strings.openAiUnsupportedSubtitle
                ServerSource.OPEN_AI_COMPATIBLE -> strings.compatibleUnsupportedSubtitle
                else -> strings.localUnsupportedSubtitle
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (mode != ServerSource.OPEN_AI_COMPATIBLE) Text(
            modifier = Modifier.padding(top = 14.dp),
            text = if (mode == ServerSource.OPEN_AI) {
                strings.openAiUnsupportedSubtitle2
            } else {
                strings.localUnsupportedSubtitle2
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Renders the `ImageToImageBody` UI for the SDAI presentation layer.
 *
 * @param state state rendered or processed by the component.
 * @param strings strings value consumed by the API.
 * @param inputImageBitmap input image bitmap value consumed by the API.
 * @param promptChipTextFieldState prompt chip text field state value consumed by the API.
 * @param negativePromptChipTextFieldState negative prompt chip text field state value consumed by the API.
 * @param processIntent process intent value consumed by the API.
 * @param onInPaintClick callback invoked by the component.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ImageToImageBody(
    state: ImageToImageState,
    strings: ImageToImageStrings,
    inputImageBitmap: ImageBitmap?,
    promptChipTextFieldState: androidx.compose.runtime.MutableState<TextFieldValue>,
    negativePromptChipTextFieldState: androidx.compose.runtime.MutableState<TextFieldValue>,
    processIntent: (ImageToImageIntent) -> Unit,
    onInPaintClick: () -> Unit,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .verticalScrollbar(listState),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ImageInputSection(
                state = state,
                strings = strings,
                image = inputImageBitmap,
                processIntent = processIntent,
                onInPaintClick = onInPaintClick,
            )
        }
        item {
            ImageToImageForm(
                state = state,
                strings = strings,
                promptChipTextFieldState = promptChipTextFieldState,
                negativePromptChipTextFieldState = negativePromptChipTextFieldState,
                processIntent = processIntent,
            )
        }

    }
}

/**
 * Renders the `ImageInputSection` UI for the SDAI presentation layer.
 *
 * @param state state rendered or processed by the component.
 * @param strings strings value consumed by the API.
 * @param image image value consumed by the API.
 * @param processIntent process intent value consumed by the API.
 * @param onInPaintClick callback invoked by the component.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ImageInputSection(
    state: ImageToImageState,
    strings: ImageToImageStrings,
    image: ImageBitmap?,
    processIntent: (ImageToImageIntent) -> Unit,
    onInPaintClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.mode.compactDisplayName(state.platform),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.W600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = strings.inputImage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (state.pickingImage) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp,
                    )
                }
            }

            if (image != null) {
                SelectedImageInput(
                    image = image,
                    inPaint = if (state.sourceSupportsInPaint) {
                        state.inPaint
                    } else {
                        ImageInPaintState()
                    },
                    strings = strings,
                    enabled = !state.generating,
                    inPaintVisible = state.sourceSupportsInPaint,
                    onInPaintClick = onInPaintClick,
                    onClearClick = { processIntent(ImageToImageIntent.ClearImageInput) },
                )
            } else {
                EmptyImageInputActions(
                    strings = strings,
                    enabled = !state.pickingImage && !state.generating,
                    onGalleryClick = { processIntent(ImageToImageIntent.PickGallery) },
                    onCameraClick = { processIntent(ImageToImageIntent.PickCamera) },
                    onRandomClick = { processIntent(ImageToImageIntent.PickRandom) },
                )
            }
        }
    }
}

/**
 * Renders the `EmptyImageInputActions` UI for the SDAI presentation layer.
 *
 * @param strings strings value consumed by the API.
 * @param enabled enabled value consumed by the API.
 * @param onGalleryClick callback invoked by the component.
 * @param onCameraClick callback invoked by the component.
 * @param onRandomClick callback invoked by the component.
 * @author Dmitriy Moroz
 */
@Composable
internal fun EmptyImageInputActions(
    strings: ImageToImageStrings,
    enabled: Boolean,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onRandomClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ImagePickerTileButton(
                modifier = Modifier.weight(1f),
                enabled = enabled,
                icon = Icons.Default.Image,
                text = strings.pickGallery,
                onClick = onGalleryClick,
            )
            ImagePickerTileButton(
                modifier = Modifier.weight(1f),
                enabled = enabled,
                icon = Icons.Default.Camera,
                text = strings.pickCamera,
                onClick = onCameraClick,
            )
        }
        ImagePickerButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            icon = Icons.Default.ArtTrack,
            text = strings.pickRandom,
            onClick = onRandomClick,
        )
    }
}

/**
 * Renders the `SelectedImageInput` UI for the SDAI presentation layer.
 *
 * @param image image value consumed by the API.
 * @param inPaint in paint value consumed by the API.
 * @param strings strings value consumed by the API.
 * @param enabled enabled value consumed by the API.
 * @param inPaintVisible whether inpaint controls are available for the active provider.
 * @param onInPaintClick callback invoked by the component.
 * @param onClearClick callback invoked by the component.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SelectedImageInput(
    image: ImageBitmap,
    inPaint: ImageInPaintState,
    strings: ImageToImageStrings,
    enabled: Boolean,
    inPaintVisible: Boolean,
    onInPaintClick: () -> Unit,
    onClearClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ImageInPaintCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
                .aspectRatio(1f),
            image = image,
            state = inPaint,
            drawEnabled = false,
            maskAlpha = 1f,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (inPaintVisible) {
                OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = enabled,
                    onClick = onInPaintClick,
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = strings.inPaint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            OutlinedButton(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = enabled,
                onClick = onClearClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = strings.clear,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
