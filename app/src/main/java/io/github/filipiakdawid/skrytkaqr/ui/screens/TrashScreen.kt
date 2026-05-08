package io.github.filipiakdawid.skrytkaqr.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.filipiakdawid.skrytkaqr.R
import io.github.filipiakdawid.skrytkaqr.ui.ParcelAction
import io.github.filipiakdawid.skrytkaqr.ui.ParcelViewModel
import io.github.filipiakdawid.skrytkaqr.ui.components.SwipeMode
import io.github.filipiakdawid.skrytkaqr.ui.components.SwipeableParcelCard

@Composable
fun TrashScreen(viewModel: ParcelViewModel) {
    val parcels by viewModel.trashParcelsUi.collectAsState()
    val lastAction by viewModel.lastAction.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources

    LaunchedEffect(lastAction) {
        lastAction?.let { action ->
            val msg =
                when (action) {
                    ParcelAction.MOVED_TO_TRASH -> resources.getString(R.string.action_moved_to_trash)
                    ParcelAction.RESTORED -> resources.getString(R.string.action_restored)
                    ParcelAction.DELETED -> resources.getString(R.string.action_deleted)
                }
            snackbarHostState.showSnackbar(msg)
            viewModel.clearLastAction()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            if (parcels.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.trash_auto_delete),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(parcels, key = { it.parcel.id }) { uiModel ->
                        SwipeableParcelCard(
                            uiModel = uiModel,
                            swipeMode = SwipeMode.TRASH,
                            onPrimarySwipe = { viewModel.deleteFromTrash(uiModel.parcel) },
                            onSecondarySwipe = { viewModel.restore(uiModel.parcel) },
                            onClick = {},
                        )
                    }
                }
            } else {
                EmptyState(
                    message = stringResource(R.string.trash_empty_message),
                    hint = stringResource(R.string.trash_auto_delete),
                )
            }
        }
    }
}
