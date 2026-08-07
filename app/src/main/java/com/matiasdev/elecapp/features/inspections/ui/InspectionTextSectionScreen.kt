package com.matiasdev.elecapp.features.inspections.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionTextSectionScreen(
    repository: InspectionRepository,
    inspectionId: String,
    section: InspectionTextSection,
    title: String,
    label: String,
    saveButtonText: String,
    savedMessage: String,
    warning: String?,
    onBackClick: () -> Unit,
    showCopyShare: Boolean = false,
    navigateBackOnSave: Boolean = false,
    showPrimarySaveButton: Boolean = true,
    onSaved: (() -> Unit)? = null,
    onPreviousClick: (() -> Unit)? = null,
    onNextClick: (() -> Unit)? = null,
    onHomeClick: (() -> Unit)? = null,
    previousLabel: String = "Atrás",
    nextLabel: String = "Siguiente",
    saveOnNextClick: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: InspectionTextSectionViewModel = viewModel(
        factory = InspectionTextSectionViewModelFactory(repository, inspectionId, section),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            when {
                onSaved != null -> onSaved()
                navigateBackOnSave -> onBackClick()
                else -> snackbarHostState.showSnackbar(savedMessage)
            }
        }
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } },
            )
        },
        bottomBar = {
            if (onPreviousClick != null && onNextClick != null) {
                InspectionSectionNavigation(
                    onPreviousClick = onPreviousClick,
                    onNextClick = {
                        if (saveOnNextClick) {
                            viewModel.save()
                        } else {
                            onNextClick()
                        }
                    },
                    onHomeClick = onHomeClick,
                    previousLabel = previousLabel,
                    nextLabel = nextLabel,
                    isNextEnabled = section == InspectionTextSection.FINAL_REPORT || uiState.status == InspectionStatus.DRAFT,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        },
    ) { padding ->
        if (uiState.isLoading) CircularProgressIndicator(Modifier.padding(padding).padding(24.dp)) else {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                warning?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                InspectionTextField(label, uiState.text, viewModel::onTextChange, minLines = 10)
                Text("Caracteres aproximados: ${uiState.characterCount}")
                if (showPrimarySaveButton) {
                    Button(
                        onClick = viewModel::save,
                        enabled = section == InspectionTextSection.FINAL_REPORT || uiState.status == InspectionStatus.DRAFT,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(saveButtonText)
                    }
                }
                if (showCopyShare) {
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(uiState.text))
                            scope.launch { snackbarHostState.showSnackbar("Informe copiado") }
                        },
                        enabled = uiState.text.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Text("Copiar informe", modifier = Modifier.padding(start = 8.dp))
                    }
                    OutlinedButton(
                        onClick = { context.sharePlainText(uiState.text) },
                        enabled = uiState.text.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Text("Compartir informe", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

private fun Context.sharePlainText(text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, "Compartir informe"))
}
