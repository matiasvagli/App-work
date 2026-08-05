package com.matiasdev.elecapp.features.settings.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.reminders.scheduling.notificationsAllowed
import com.matiasdev.elecapp.features.settings.data.ReminderSettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: ReminderSettingsRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(repository)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Notificaciones", style = MaterialTheme.typography.titleMedium)
            Text(if (notificationsAllowed(context)) "Permiso concedido" else "Permiso pendiente o deshabilitado")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                OutlinedButton(onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                    Text("Solicitar permiso")
                }
            }
            Text("Recordatorio predeterminado", style = MaterialTheme.typography.titleMedium)
            Presets(onSelect = viewModel::onFirstReminderChange)
            OutlinedTextField(
                value = uiState.firstReminderMinutes,
                onValueChange = viewModel::onFirstReminderChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Primer recordatorio en minutos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Segundo recordatorio predeterminado")
                Switch(uiState.secondReminderEnabled, onCheckedChange = viewModel::onSecondReminderEnabledChange)
            }
            if (uiState.secondReminderEnabled) {
                OutlinedTextField(
                    value = uiState.secondReminderMinutes,
                    onValueChange = viewModel::onSecondReminderChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Segundo recordatorio en minutos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar")
            }
            uiState.savedMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            Text(
                "Android puede demorar recordatorios no exactos por ahorro de batería.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Presets(onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { onSelect("") }) { Text("Ninguno") }
        OutlinedButton(onClick = { onSelect("30") }) { Text("30m") }
        OutlinedButton(onClick = { onSelect("60") }) { Text("1h") }
        OutlinedButton(onClick = { onSelect("1440") }) { Text("1d") }
    }
}
