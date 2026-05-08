package io.github.filipiakdawid.skrytkaqr.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.filipiakdawid.skrytkaqr.R
import io.github.filipiakdawid.skrytkaqr.ui.ParcelViewModel
import io.github.filipiakdawid.skrytkaqr.util.AppPreferences
import io.github.filipiakdawid.skrytkaqr.util.SmsParser

@Composable
fun SettingsScreen(
    viewModel: ParcelViewModel,
    onNavigateToAbout: () -> Unit,
) {
    var phone by remember { mutableStateOf(viewModel.phoneNumber.value) }
    var sender by remember { mutableStateOf(viewModel.senderFilter.value) }
    var codeRegex by remember { mutableStateOf(viewModel.codeRegex.value) }
    var lockerRegex by remember { mutableStateOf(viewModel.lockerRegex.value) }
    var smsSample by remember { mutableStateOf(viewModel.smsSample.value) }
    var advExpanded by remember { mutableStateOf(false) }

    val codeRegexValid = SmsParser.isValidRegex(codeRegex)
    val lockerRegexValid = SmsParser.isValidRegex(lockerRegex)
    val detectedCode = SmsParser.testCodeRegex(codeRegex, smsSample)
    val lockerMatches = SmsParser.testLockerRegex(lockerRegex, smsSample)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_section_basic),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        TextField(
            value = phone,
            onValueChange = {
                phone = it
                viewModel.setPhoneNumber(it)
            },
            label = { Text(stringResource(R.string.settings_phone_label)) },
            placeholder = { Text(stringResource(R.string.settings_phone_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        TextField(
            value = sender,
            onValueChange = {
                sender = it
                viewModel.setSenderFilter(it)
            },
            label = { Text(stringResource(R.string.settings_sender_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        HorizontalDivider()

        TextButton(
            onClick = { advExpanded = !advExpanded },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.settings_advanced_section))
            Spacer(Modifier.weight(1f))
            Icon(
                if (advExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
            )
        }

        if (advExpanded) {
            OutlinedTextField(
                value = codeRegex,
                onValueChange = {
                    codeRegex = it
                    viewModel.setCodeRegex(it)
                },
                label = { Text(stringResource(R.string.settings_code_regex_label)) },
                modifier = Modifier.fillMaxWidth(),
                isError = !codeRegexValid,
                supportingText = {
                    when {
                        !codeRegexValid -> Text(stringResource(R.string.settings_regex_invalid))
                        detectedCode != null ->
                            Text(
                                stringResource(R.string.settings_code_found, detectedCode),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        else -> Text(stringResource(R.string.settings_regex_no_match))
                    }
                },
            )
            OutlinedTextField(
                value = lockerRegex,
                onValueChange = {
                    lockerRegex = it
                    viewModel.setLockerRegex(it)
                },
                label = { Text(stringResource(R.string.settings_locker_regex_label)) },
                modifier = Modifier.fillMaxWidth(),
                isError = !lockerRegexValid,
                supportingText = {
                    when {
                        !lockerRegexValid -> Text(stringResource(R.string.settings_regex_invalid))
                        lockerMatches ->
                            Text(
                                stringResource(R.string.settings_locker_match),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        else -> Text(stringResource(R.string.settings_regex_no_match))
                    }
                },
            )
            OutlinedTextField(
                value = smsSample,
                onValueChange = {
                    smsSample = it
                    viewModel.setSmsSample(it)
                },
                label = { Text(stringResource(R.string.settings_sms_sample_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = {
                    viewModel.resetAdvancedToDefaults()
                    codeRegex = AppPreferences.DEFAULT_CODE_REGEX
                    lockerRegex = AppPreferences.DEFAULT_LOCKER_REGEX
                    smsSample = AppPreferences.DEFAULT_SMS_SAMPLE
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_reset_button))
            }
        }

        HorizontalDivider()

        TextButton(
            onClick = onNavigateToAbout,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.settings_about_button))
        }
    }
}
