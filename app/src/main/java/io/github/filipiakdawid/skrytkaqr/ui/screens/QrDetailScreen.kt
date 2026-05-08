package io.github.filipiakdawid.skrytkaqr.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.filipiakdawid.skrytkaqr.R
import io.github.filipiakdawid.skrytkaqr.ui.ParcelViewModel

@Composable
fun QrDetailScreen(
    parcelId: Long,
    viewModel: ParcelViewModel,
    onTrashed: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val resources = LocalContext.current.resources

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        activity?.window?.attributes =
            activity?.window?.attributes?.also {
                it.screenBrightness = 1.0f
            }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.window?.attributes =
                activity?.window?.attributes?.also {
                    it.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                }
        }
    }

    if (phoneNumber.isBlank()) {
        NoPhoneWarning(onNavigateToSettings = onNavigateToSettings)
        return
    }

    val parcel =
        remember(parcelId) {
            viewModel.activeParcelsUi.value.find { it.parcel.id == parcelId }
        }

    if (parcel == null) {
        EmptyState(
            message = stringResource(R.string.parcel_not_found),
            hint = "",
        )
        return
    }

    val bitmap = remember(parcel.parcel.code) { viewModel.generateQrBitmap(parcel.parcel.code, 900) }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.qr_content_description, parcel.parcel.code),
                modifier =
                    Modifier
                        .fillMaxWidth(0.90f)
                        .aspectRatio(1f),
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = parcel.parcel.code,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
            )

            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                )
            }

            if (parcel.parcel.parcelCount > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier =
                        Modifier
                            .background(
                                color = Color(0xFFE3F2FD),
                                shape = MaterialTheme.shapes.small,
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.AllInbox,
                        contentDescription = null,
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = resources.getQuantityString(R.plurals.packages_count, parcel.parcel.parcelCount, parcel.parcel.parcelCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            if (parcel.parcel.location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = parcel.parcel.location,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                )
            }
            if (parcel.parcel.expiryDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text =
                        stringResource(
                            R.string.parcel_valid_until_qr,
                            (parcel.parcel.expiryDate + " " + parcel.parcel.expiryTime).trim(),
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                )
            }
        }

        Button(
            onClick = {
                viewModel.moveToTrash(parcel.parcel)
                onTrashed()
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.qr_trash_button))
        }
    }
}
