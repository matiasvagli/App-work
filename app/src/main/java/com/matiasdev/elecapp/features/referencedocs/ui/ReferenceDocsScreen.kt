package com.matiasdev.elecapp.features.referencedocs.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.external.browserIntent
import com.matiasdev.elecapp.core.external.tryStartActivity
import com.matiasdev.elecapp.core.external.viewPdfIntent
import com.matiasdev.elecapp.core.ui.theme.ElecAppTheme
import com.matiasdev.elecapp.features.referencedocs.data.ReferenceDocumentRepository
import com.matiasdev.elecapp.features.referencedocs.data.ReferenceDocumentStorage
import com.matiasdev.elecapp.features.referencedocs.domain.AaiericLaborCostsSource

private val PDF_MIME_TYPES = arrayOf("application/pdf")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceDocsScreen(
    repository: ReferenceDocumentRepository,
    storage: ReferenceDocumentStorage,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ReferenceDocsViewModel = viewModel(
        factory = ReferenceDocsViewModelFactory(repository, storage),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // El selector no informa para qué fuente se abrió, así que el origen se recuerda acá.
    var pendingSourceUrl by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val sourceUrl = pendingSourceUrl
        pendingSourceUrl = null
        if (uri != null) viewModel.onImport(uri, sourceUrl)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ReferenceDocsEvent.OpenPdf -> {
                    if (!context.tryStartActivity(viewPdfIntent(event.uri))) {
                        snackbarHostState.showSnackbar("No hay ninguna app instalada para abrir PDF")
                    }
                }

                is ReferenceDocsEvent.OpenUrl -> {
                    if (!context.tryStartActivity(browserIntent(event.url))) {
                        snackbarHostState.showSnackbar("No se pudo abrir el navegador")
                    }
                }

                is ReferenceDocsEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Documentos de consulta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        ReferenceDocsContent(
            uiState = uiState,
            onSourceClick = viewModel::onSourceClick,
            onImportClick = { sourceUrl ->
                pendingSourceUrl = sourceUrl
                importLauncher.launch(PDF_MIME_TYPES)
            },
            onDocumentClick = viewModel::onOpen,
            onDeleteConfirm = viewModel::onDelete,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun ReferenceDocsContent(
    uiState: ReferenceDocsUiState,
    onSourceClick: (String) -> Unit,
    onImportClick: (String?) -> Unit,
    onDocumentClick: (String) -> Unit,
    onDeleteConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<ReferenceDocumentRow?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ReferenceSourceCard(
                source = AaiericLaborCostsSource,
                isImporting = uiState.isImporting,
                onOpenSite = { onSourceClick(AaiericLaborCostsSource.url) },
                onImport = { onImportClick(AaiericLaborCostsSource.url) },
            )
        }

        item {
            Text(
                text = "Mis documentos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        when {
            uiState.isLoading -> item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }

            uiState.documents.isEmpty() -> item {
                EmptyReferenceDocsCard(onImport = { onImportClick(null) })
            }

            else -> {
                items(uiState.documents, key = { it.id }) { document ->
                    ReferenceDocumentCard(
                        document = document,
                        onClick = { onDocumentClick(document.id) },
                        onDeleteClick = { pendingDelete = document },
                    )
                }
                item {
                    OutlinedButton(onClick = { onImportClick(null) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importar otro PDF")
                    }
                }
            }
        }
    }

    pendingDelete?.let { document ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Eliminar documento") },
            text = {
                Text(
                    "Se va a borrar \"${document.title}\" de la app. " +
                        "Vas a tener que importarlo de nuevo para volver a consultarlo.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteConfirm(document.id)
                        pendingDelete = null
                    },
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReferenceDocsEmptyPreview() {
    ElecAppTheme {
        ReferenceDocsContent(
            uiState = ReferenceDocsUiState(isLoading = false),
            onSourceClick = {},
            onImportClick = {},
            onDocumentClick = {},
            onDeleteConfirm = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReferenceDocsWithDocumentsPreview() {
    ElecAppTheme {
        ReferenceDocsContent(
            uiState = ReferenceDocsUiState(
                isLoading = false,
                documents = listOf(
                    ReferenceDocumentRow(
                        id = "1",
                        title = "AAIERIC - Costos Sugeridos de Mano de Obra - Julio 2026",
                        sizeLabel = "617 kB",
                        ageLabel = "Importado hace 3 días",
                        isStale = false,
                        sourceUrl = AaiericLaborCostsSource.url,
                    ),
                    ReferenceDocumentRow(
                        id = "2",
                        title = "AAIERIC - Costos Sugeridos de Mano de Obra - Abril 2026",
                        sizeLabel = "610 kB",
                        ageLabel = "Importado hace 3 meses",
                        isStale = true,
                        sourceUrl = AaiericLaborCostsSource.url,
                    ),
                ),
            ),
            onSourceClick = {},
            onImportClick = {},
            onDocumentClick = {},
            onDeleteConfirm = {},
        )
    }
}
