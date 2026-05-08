package io.github.filipiakdawid.skrytkaqr.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.filipiakdawid.skrytkaqr.R
import io.github.filipiakdawid.skrytkaqr.ui.ExpiryState
import io.github.filipiakdawid.skrytkaqr.ui.ParcelUiModel

enum class SwipeMode { ACTIVE, TRASH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableParcelCard(
    uiModel: ParcelUiModel,
    swipeMode: SwipeMode = SwipeMode.ACTIVE,
    onPrimarySwipe: () -> Unit,
    onSecondarySwipe: () -> Unit,
    onClick: () -> Unit,
) {
    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                when (value) {
                    SwipeToDismissBoxValue.EndToStart -> {
                        onPrimarySwipe()
                        true
                    }
                    SwipeToDismissBoxValue.StartToEnd -> {
                        if (swipeMode == SwipeMode.TRASH) {
                            onSecondarySwipe()
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            },
            positionalThreshold = { it * 0.35f },
        )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = swipeMode == SwipeMode.TRASH,
        backgroundContent = {
            SwipeBackground(
                direction = dismissState.dismissDirection,
                progress = dismissState.progress,
                swipeMode = swipeMode,
            )
        },
        content = {
            ParcelCard(uiModel = uiModel, onClick = onClick)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(
    direction: SwipeToDismissBoxValue,
    progress: Float,
    swipeMode: SwipeMode,
) {
    val isEndToStart = direction == SwipeToDismissBoxValue.EndToStart
    val isStartToEnd = direction == SwipeToDismissBoxValue.StartToEnd

    val targetColor =
        when {
            isEndToStart -> MaterialTheme.colorScheme.errorContainer
            isStartToEnd && swipeMode == SwipeMode.TRASH -> Color(0xFF1B5E20).copy(alpha = 0.3f)
            else -> Color.Transparent
        }
    val animatedAlpha by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "swipe_alpha",
    )
    val color = targetColor.copy(alpha = targetColor.alpha * animatedAlpha)

    val icon =
        when {
            isEndToStart -> Icons.Default.Delete
            isStartToEnd && swipeMode == SwipeMode.TRASH -> Icons.Default.RestoreFromTrash
            else -> null
        }
    val alignment = if (isEndToStart) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium)
                .background(color)
                .padding(horizontal = 20.dp),
        contentAlignment = alignment,
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = animatedAlpha),
            )
        }
    }
}

@Composable
fun ParcelCard(
    uiModel: ParcelUiModel,
    onClick: () -> Unit,
) {
    val parcel = uiModel.parcel

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.parcel_code_label, parcel.code),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (parcel.parcelCount > 1) {
                            Icon(
                                imageVector = Icons.Default.AllInbox,
                                contentDescription = stringResource(R.string.parcel_multi_locker),
                                tint = Color(0xFF1565C0),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    if (parcel.expiryDate.isNotEmpty()) {
                        ExpiryChip(uiModel = uiModel)
                    }
                }

                if (parcel.location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = parcel.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (parcel.parcelCount > 1) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(22.dp)
                            .background(color = Color(0xFF1565C0), shape = CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${parcel.parcelCount}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpiryChip(uiModel: ParcelUiModel) {
    val parcel = uiModel.parcel

    val chipLabel =
        if (parcel.expiryTime.isNotEmpty()) {
            "${parcel.expiryDate} ${parcel.expiryTime}"
        } else {
            parcel.expiryDate
        }

    val icon: ImageVector? =
        when (uiModel.expiryState) {
            ExpiryState.WARNING -> Icons.Default.Warning
            ExpiryState.EXPIRED -> Icons.Default.Error
            ExpiryState.NORMAL -> null
        }

    val chipColors =
        when (uiModel.expiryState) {
            ExpiryState.EXPIRED ->
                SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    labelColor = MaterialTheme.colorScheme.onErrorContainer,
                    iconContentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            else -> SuggestionChipDefaults.suggestionChipColors()
        }

    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text =
                    if (uiModel.expiryState == ExpiryState.EXPIRED) {
                        stringResource(R.string.parcel_expired)
                    } else {
                        stringResource(R.string.parcel_valid_until, chipLabel)
                    },
                style = MaterialTheme.typography.labelSmall,
            )
        },
        icon =
            icon?.let {
                { Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(16.dp)) }
            },
        colors = chipColors,
    )
}
