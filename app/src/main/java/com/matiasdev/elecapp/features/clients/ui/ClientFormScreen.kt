package com.matiasdev.elecapp.features.clients.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.external.readImportedVCard
import com.matiasdev.elecapp.core.external.readImportedContact
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormScreen(
    repository: ClientRepository,
    clientId: String?,
    initialDraft: ClientFormDraft = ClientFormDraft(),
    onBackClick: () -> Unit,
    onSaved: (String) -> Unit,
    saveButtonText: String = "Guardar",
    showScheduleAfterSave: Boolean = false,
    onScheduleVisitClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ClientFormViewModel = viewModel(
        factory = ClientFormViewModelFactory(repository, clientId, initialDraft),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedNavigationHandled = remember { androidx.compose.runtime.mutableStateOf(false) }
    val contactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
    ) { uri ->
        if (uri == null) {
            viewModel.onContactSelectionCancelled()
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                readImportedContact(context.contentResolver, uri)
            }
            result
                .onSuccess(viewModel::applyImportedContact)
                .onFailure { viewModel.onContactReadFailed() }
        }
    }
    val vCardLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) {
            viewModel.onVCardSelectionCancelled()
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                readImportedVCard(context.contentResolver, uri)
            }
            result
                .onSuccess(viewModel::applyImportedContact)
                .onFailure { viewModel.onVCardReadFailed() }
        }
    }

    LaunchedEffect(uiState.savedClientId) {
        val savedClientId = uiState.savedClientId
        if (savedClientId != null && !showScheduleAfterSave && !savedNavigationHandled.value) {
            savedNavigationHandled.value = true
            onSaved(savedClientId)
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        val message = uiState.errorMessage ?: uiState.successMessage
        if (message != null) snackbarHostState.showSnackbar(message)
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (clientId == null) "Nuevo cliente" else "Editar cliente") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (clientId == null) {
                        IconButton(onClick = { contactLauncher.launch(null) }) {
                            Icon(Icons.Default.ContactPhone, contentDescription = "Importar desde contactos")
                        }
                        IconButton(
                            onClick = {
                                vCardLauncher.launch(
                                    arrayOf("text/x-vcard", "text/vcard", "text/directory", "text/plain", "application/octet-stream", "*/*"),
                                )
                            },
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Importar archivo vCard")
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            ClientFormContent(
                uiState = uiState,
                onFullNameChange = viewModel::onFullNameChange,
                onPhoneChange = viewModel::onPhoneChange,
                onEmailChange = viewModel::onEmailChange,
                onAddressChange = viewModel::onAddressChange,
                onLocalityChange = viewModel::onLocalityChange,
                onNotesChange = viewModel::onNotesChange,
                onSaveClick = viewModel::save,
                onCancelClick = onBackClick,
                saveButtonText = saveButtonText,
                showScheduleAfterSave = showScheduleAfterSave,
                onScheduleVisitClick = onScheduleVisitClick,
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (uiState.phoneChoices.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = viewModel::dismissPhoneChoices,
            title = { Text("Elegí un teléfono") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.phoneChoices.forEach { phone ->
                        TextButton(onClick = { viewModel.selectImportedPhone(phone) }) {
                            Text(phone)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = viewModel::dismissPhoneChoices) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun ClientFormContent(
    uiState: ClientFormUiState,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onLocalityChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    saveButtonText: String,
    showScheduleAfterSave: Boolean,
    onScheduleVisitClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        OutlinedTextField(
            value = uiState.fullName,
            onValueChange = onFullNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre completo") },
            singleLine = true,
            isError = uiState.fullNameError != null,
            supportingText = uiState.fullNameError?.let { { Text(it) } },
        )
        OutlinedTextField(
            value = uiState.phone,
            onValueChange = onPhoneChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Teléfono") },
            singleLine = true,
            isError = uiState.phoneError != null,
            supportingText = uiState.phoneError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        OutlinedTextField(
            value = uiState.address,
            onValueChange = onAddressChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Dirección") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )
        OutlinedTextField(
            value = uiState.locality,
            onValueChange = onLocalityChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Localidad") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )
        OutlinedTextField(
            value = uiState.notes,
            onValueChange = onNotesChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notas") },
            minLines = 4,
        )

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        uiState.successMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving,
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator()
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Text(
                    text = saveButtonText,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        if (showScheduleAfterSave && uiState.savedClientId != null) {
            Button(
                onClick = { onScheduleVisitClick(uiState.savedClientId) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Agendar una visita")
            }
        }
        OutlinedButton(
            onClick = onCancelClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Cancelar")
        }
    }
}
