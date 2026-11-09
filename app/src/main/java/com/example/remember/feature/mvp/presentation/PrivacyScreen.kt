package com.example.remember.feature.mvp.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.remember.R
import com.example.remember.feature.mvp.domain.MvpState
import com.example.remember.ui.components.NeomorphicButton
import com.example.remember.ui.components.NeomorphicCard
import com.example.remember.ui.components.neomorphicInset
import com.example.remember.ui.theme.NeoBackground
import com.example.remember.ui.theme.NeoButton
import com.example.remember.ui.theme.NeoText

@Composable
fun PrivacyScreen(
    state: MvpState,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onUpdateProfileName: (String) -> Unit,
    onExportData: () -> Unit,
    onOpenContact: () -> Unit,
    onDeleteAllData: () -> Unit
) {
    var profileName by rememberSaveable(state.profileName) { mutableStateOf(state.profileName) }
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = contentPadding.calculateTopPadding() + 24.dp,
            end = 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { PrivacyBackHeader(onBack) }
        item {
            Text(
                stringResource(R.string.privacy_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.privacy_notice_version),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            PrivacySection(
                R.string.privacy_overview_title,
                R.string.privacy_overview_text
            )
        }
        item {
            PrivacySection(
                R.string.privacy_controller_title,
                R.string.privacy_controller_text
            ) {
                Text(
                    stringResource(R.string.privacy_contact_url),
                    color = NeoButton,
                    style = MaterialTheme.typography.labelMedium
                )
                NeomorphicButton(
                    onClick = onOpenContact,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.privacy_contact_action))
                }
            }
        }
        item { PrivacySection(R.string.privacy_data_title, R.string.privacy_data_text) }
        item { PrivacySection(R.string.privacy_purpose_title, R.string.privacy_purpose_text) }
        item { PrivacySection(R.string.privacy_storage_title, R.string.privacy_storage_text) }
        item { PrivacySection(R.string.privacy_permissions_title, R.string.privacy_permissions_text) }
        item { PrivacySection(R.string.privacy_sharing_title, R.string.privacy_sharing_text) }
        item {
            PrivacySection(R.string.privacy_rights_title, R.string.privacy_rights_text) {
                Text(
                    stringResource(
                        R.string.privacy_stored_summary,
                        state.profileName,
                        state.goals.size,
                        state.completionHistory.size,
                        state.unlockedAchievements.size,
                        state.ownedBundleIds.size
                    ),
                    color = NeoText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        item {
            PrivacySection(
                R.string.privacy_correct_name_title,
                R.string.privacy_correct_name_description
            ) {
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .neomorphicInset(RoundedCornerShape(16.dp)),
                    label = { Text(stringResource(R.string.privacy_profile_name_label)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                NeomorphicButton(
                    onClick = { onUpdateProfileName(profileName) },
                    enabled = profileName.isNotBlank() && profileName.trim() != state.profileName,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.privacy_save_name))
                }
            }
        }
        item {
            PrivacySection(
                R.string.privacy_export_title,
                R.string.privacy_export_description
            ) {
                NeomorphicButton(
                    onClick = onExportData,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.privacy_export_action))
                }
            }
        }
        item {
            PrivacySection(
                R.string.privacy_delete_title,
                R.string.privacy_delete_description
            ) {
                NeomorphicButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF8B2E3C)
                ) {
                    Text(stringResource(R.string.privacy_delete_action))
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = NeoBackground,
            title = { Text(stringResource(R.string.privacy_delete_confirm_title)) },
            text = { Text(stringResource(R.string.privacy_delete_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    onDeleteAllData()
                }) {
                    Text(
                        stringResource(R.string.privacy_delete_confirm_action),
                        color = Color(0xFF8B2E3C)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.privacy_cancel_action), color = NeoButton)
                }
            }
        )
    }
}

@Composable
private fun PrivacyBackHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack) {
            Text("‹", style = MaterialTheme.typography.headlineMedium, color = NeoButton)
        }
        Text(
            stringResource(R.string.privacy_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = NeoText
        )
    }
}

@Composable
private fun PrivacySection(
    title: Int,
    description: Int,
    content: @Composable (() -> Unit)? = null
) {
    NeomorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeoText
            )
            Text(
                stringResource(description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content?.invoke()
        }
    }
}
