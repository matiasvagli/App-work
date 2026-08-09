package com.matiasdev.elecapp.features.clients.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.external.readImportedContact
import com.matiasdev.elecapp.core.external.readImportedVCard
import com.matiasdev.elecapp.core.ui.components.ElecLoadingState
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
    val savedNavigationHandled = remember { mutableStateOf(false) }

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
    val contactPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            contactLauncher.launch(null)
        } else {
            viewModel.onContactPermissionDenied()
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
                title = { Text(if (clientId == null) "Nuevo cliente" else "Editar cliente", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (clientId == null) {
                        IconButton(
                            onClick = {
                                if (
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
                                    PackageManager.PERMISSION_GRANTED
                                ) {
                                    contactLauncher.launch(null)
                                } else {
                                    contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                }
                            },
                        ) {
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            ElecLoadingState("Cargando datos del cliente...")
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
            title = { Text("Elegí un teléfono", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.phoneChoices.forEach { phone ->
                        TextButton(
                            onClick = { viewModel.selectImportedPhone(phone) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(phone, style = MaterialTheme.typography.bodyLarge)
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Datos de contacto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                OutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = onFullNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre completo *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    isError = uiState.fullNameError != null,
                    supportingText = uiState.fullNameError?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp),
                )

                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = onPhoneChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Teléfono *") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    isError = uiState.phoneError != null,
                    supportingText = uiState.phoneError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                )

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Ubicación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                OutlinedTextField(
                    value = uiState.address,
                    onValueChange = onAddressChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Dirección") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                OutlinedTextField(
                    value = uiState.locality,
                    onValueChange = onLocalityChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Localidad") },
                    leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Notas adicionales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notas o detalles relevantes") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                )
            }
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving,
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = saveButtonText,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        if (showScheduleAfterSave && uiState.savedClientId != null) {
            FilledTonalButton(
                onClick = { onScheduleVisitClick(uiState.savedClientId) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agendar una visita", fontWeight = FontWeight.Bold)
            }
        }

        OutlinedButton(
            onClick = onCancelClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            Text("Cancelar", fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

