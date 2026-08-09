package com.matiasdev.elecapp.features.visits.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.reminders.data.VisitReminderRepository
import com.matiasdev.elecapp.features.reminders.domain.ReminderOption
import com.matiasdev.elecapp.features.reminders.domain.ReminderUnit
import com.matiasdev.elecapp.features.reminders.domain.reminderSummary
import com.matiasdev.elecapp.features.reminders.scheduling.ReminderCoordinator
import com.matiasdev.elecapp.features.reminders.scheduling.notificationsAllowed
import com.matiasdev.elecapp.features.settings.data.ReminderSettingsRepository
import com.matiasdev.elecapp.features.visits.data.VisitRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitFormScreen(
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    reminderRepository: VisitReminderRepository,
    settingsRepository: ReminderSettingsRepository,
    reminderCoordinator: ReminderCoordinator,
    clientId: String?,
    visitId: String? = null,
    returnedClientId: String? = null,
    onBackClick: () -> Unit,
    onCreateClientClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VisitFormViewModel = viewModel(
        factory = VisitFormViewModelFactory(
            clientRepository,
            visitRepository,
            reminderRepository,
            settingsRepository,
            reminderCoordinator,
            clientId,
            visitId,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {}

    LaunchedEffect(returnedClientId) {
        returnedClientId?.let(viewModel::selectClientById)
    }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }

    LaunchedEffect(uiState.errorMessage, uiState.reminderWarning) {
        (uiState.errorMessage ?: uiState.reminderWarning)?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (visitId == null) "Agendar visita" else "Editar visita") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            VisitFormContent(
                uiState = uiState,
                onCreateClientClick = onCreateClientClick,
                onNotificationPermissionNeeded = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsAllowed(context)) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                actions = VisitFormActions(
                    onClientSearchChange = viewModel::onClientSearchChange,
                    onSelectClient = viewModel::selectClient,
                    onChangeClient = viewModel::clearClientSelection,
                    onDateChange = viewModel::onDateChange,
                    onTimeChange = viewModel::onTimeChange,
                    onDurationChange = viewModel::onDurationChange,
                    onReasonChange = viewModel::onReasonChange,
                    onNotesChange = viewModel::onNotesChange,
                    onFirstReminderOptionChange = viewModel::onFirstReminderOptionChange,
                    onFirstReminderCustomValueChange = viewModel::onFirstReminderCustomValueChange,
                    onFirstReminderUnitChange = viewModel::onFirstReminderUnitChange,
                    onAddSecondReminder = viewModel::addSecondReminder,
                    onRemoveSecondReminder = viewModel::removeSecondReminder,
                    onSecondReminderOptionChange = viewModel::onSecondReminderOptionChange,
                    onSecondReminderCustomValueChange = viewModel::onSecondReminderCustomValueChange,
                    onSecondReminderUnitChange = viewModel::onSecondReminderUnitChange,
                    onSaveClick = viewModel::save,
                ),
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun VisitFormContent(
    uiState: VisitFormUiState,
    onCreateClientClick: () -> Unit,
    onNotificationPermissionNeeded: () -> Unit,
    actions: VisitFormActions,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        VisitClientSelector(
            selectedClient = uiState.client,
            searchQuery = uiState.clientSearchQuery,
            searchResults = uiState.clientSearchResults,
            isLoading = uiState.isClientSearchLoading,
            onSearchChange = actions.onClientSearchChange,
            onSelectClient = actions.onSelectClient,
            onChangeClient = actions.onChangeClient,
            onCreateClientClick = onCreateClientClick,
        )

        DateTimeFields(uiState, actions)

        // Work Details Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = "Detalles del Trabajo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                OutlinedTextField(
                    value = uiState.reason,
                    onValueChange = actions.onReasonChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Motivo de la visita") },
                    singleLine = true,
                    isError = uiState.reasonError != null,
                    supportingText = uiState.reasonError?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp),
                )

                OutlinedTextField(
                    value = uiState.durationMinutes,
                    onValueChange = actions.onDurationChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Duración estimada (minutos)") },
                    singleLine = true,
                    isError = uiState.durationError != null,
                    supportingText = uiState.durationError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                )

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = actions.onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notas o descripción adicional") },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                )
            }
        }

        ReminderFields(uiState, notificationsAllowed(context), onNotificationPermissionNeeded, actions)

        uiState.errorMessage?.let { ErrorText(it) }

        Button(
            onClick = actions.onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            enabled = !uiState.isSaving && uiState.hasRequiredData,
            shape = RoundedCornerShape(12.dp),
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Text("Guardar Visita", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DateTimeFields(uiState: VisitFormUiState, actions: VisitFormActions) {
    val context = LocalContext.current
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(
                    text = "Fecha y Hora de Visita",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, day -> actions.onDateChange(java.time.LocalDate.of(year, month + 1, day)) },
                            uiState.date.year,
                            uiState.date.monthValue - 1,
                            uiState.date.dayOfMonth,
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("📅 ${uiState.date.formatVisitDate()}", fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute -> actions.onTimeChange(java.time.LocalTime.of(hour, minute)) },
                            uiState.time.hour,
                            uiState.time.minute,
                            true,
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("⏰ ${uiState.time.formatVisitTime()}", fontWeight = FontWeight.SemiBold)
                }
            }
            uiState.dateTimeError?.let { ErrorText(it) }
        }
    }
}

@Composable
private fun ReminderFields(
    uiState: VisitFormUiState,
    notificationsAreAllowed: Boolean,
    onNotificationPermissionNeeded: () -> Unit,
    actions: VisitFormActions,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Recordatorios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            if (!notificationsAreAllowed) {
                Text(
                    text = "Las notificaciones están deshabilitadas. Se solicitará permiso al guardar recordatorios.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ReminderSelectorField(
                label = "Primer recordatorio",
                value = uiState.firstReminder,
                onOptionChange = {
                    if (it != ReminderOption.NONE) onNotificationPermissionNeeded()
                    actions.onFirstReminderOptionChange(it)
                },
                onCustomValueChange = actions.onFirstReminderCustomValueChange,
                onUnitChange = actions.onFirstReminderUnitChange,
            )
            if (uiState.secondReminderEnabled) {
                ReminderSelectorField(
                    label = "Segundo recordatorio",
                    value = uiState.secondReminder,
                    onOptionChange = {
                        if (it != ReminderOption.NONE) onNotificationPermissionNeeded()
                        actions.onSecondReminderOptionChange(it)
                    },
                    onCustomValueChange = actions.onSecondReminderCustomValueChange,
                    onUnitChange = actions.onSecondReminderUnitChange,
                    onRemove = actions.onRemoveSecondReminder,
                )
            } else {
                OutlinedButton(
                    onClick = actions.onAddSecondReminder,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                ) {
                    Text("+ Agregar segundo recordatorio")
                }
            }
            Text(
                text = "Resumen: ${uiState.reminderSummaryText()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            uiState.reminderError?.let { ErrorText(it) }
        }
    }
}

private data class VisitFormActions(
    val onClientSearchChange: (String) -> Unit,
    val onSelectClient: (com.matiasdev.elecapp.features.clients.domain.Client) -> Unit,
    val onChangeClient: () -> Unit,
    val onDateChange: (java.time.LocalDate) -> Unit,
    val onTimeChange: (java.time.LocalTime) -> Unit,
    val onDurationChange: (String) -> Unit,
    val onReasonChange: (String) -> Unit,
    val onNotesChange: (String) -> Unit,
    val onFirstReminderOptionChange: (ReminderOption) -> Unit,
    val onFirstReminderCustomValueChange: (String) -> Unit,
    val onFirstReminderUnitChange: (ReminderUnit) -> Unit,
    val onAddSecondReminder: () -> Unit,
    val onRemoveSecondReminder: () -> Unit,
    val onSecondReminderOptionChange: (ReminderOption) -> Unit,
    val onSecondReminderCustomValueChange: (String) -> Unit,
    val onSecondReminderUnitChange: (ReminderUnit) -> Unit,
    val onSaveClick: () -> Unit,
)

private fun VisitFormUiState.reminderSummaryText(): String {
    return reminderSummary(listOfNotNull(firstReminder.toMinutesOrNull(), secondReminder.toMinutesOrNull().takeIf { secondReminderEnabled }))
}

@Composable
private fun ErrorText(message: String) {
    Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
}
