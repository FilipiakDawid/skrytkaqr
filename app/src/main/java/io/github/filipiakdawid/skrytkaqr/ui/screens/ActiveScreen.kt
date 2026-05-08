package io.github.filipiakdawid.skrytkaqr.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.filipiakdawid.skrytkaqr.R
import io.github.filipiakdawid.skrytkaqr.ui.ParcelAction
import io.github.filipiakdawid.skrytkaqr.ui.ParcelUiModel
import io.github.filipiakdawid.skrytkaqr.ui.ParcelViewModel
import io.github.filipiakdawid.skrytkaqr.ui.components.SwipeMode
import io.github.filipiakdawid.skrytkaqr.ui.components.SwipeableParcelCard
import kotlinx.coroutines.launch

@Composable
fun ActiveScreen(
    viewModel: ParcelViewModel,
    onParcelClick: (ParcelUiModel) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val isSyncing by viewModel.isSyncing.collectAsState()
    val parcels by viewModel.activeParcelsUi.collectAsState()
    val syncResult by viewModel.lastSyncResult.collectAsState()
    val lastAction by viewModel.lastAction.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources
    val rotation = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(syncResult) {
        syncResult?.let { result ->
            val msg =
                when {
                    result.imported == 0 && result.updated == 0 ->
                        resources.getString(R.string.active_sync_none)
                    result.imported > 0 && result.updated > 0 ->
                        resources.getQuantityString(R.plurals.sync_imported, result.imported, result.imported) +
                            ", " +
                            resources.getQuantityString(R.plurals.sync_updated, result.updated, result.updated).lowercase()
                    result.imported > 0 ->
                        resources.getQuantityString(R.plurals.sync_imported, result.imported, result.imported)
                    else ->
                        resources.getQuantityString(R.plurals.sync_updated, result.updated, result.updated)
                }
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSyncResult()
        }
    }

    LaunchedEffect(lastAction) {
        lastAction?.let { action ->
            val msg =
                when (action) {
                    ParcelAction.MOVED_TO_TRASH -> resources.getString(R.string.action_moved_to_trash)
                    ParcelAction.RESTORED -> resources.getString(R.string.action_restored)
                    ParcelAction.DELETED -> resources.getString(R.string.action_deleted)
                }
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
            viewModel.clearLastAction()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (!isSyncing) {
                    viewModel.syncSms()
                    coroutineScope.launch {
                        rotation.animateTo(
                            targetValue = rotation.value + 360f,
                            animationSpec = tween(1000, easing = LinearEasing),
                        )
                    }
                }
            }) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.active_sync_fab),
                    modifier = Modifier.rotate(rotation.value),
                )
            }
        },
    ) { padding ->
        if (parcels.isEmpty()) {
            EmptyState(
                modifier = Modifier.padding(padding),
                message = stringResource(R.string.active_empty_message),
                hint = stringResource(R.string.active_empty_hint),
            )
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(parcels, key = { it.parcel.id }) { uiModel ->
                    SwipeableParcelCard(
                        uiModel = uiModel,
                        swipeMode = SwipeMode.ACTIVE,
                        onPrimarySwipe = { viewModel.moveToTrash(uiModel.parcel) },
                        onSecondarySwipe = {},
                        onClick = { onParcelClick(uiModel) },
                    )
                }
            }
        }
    }
}
