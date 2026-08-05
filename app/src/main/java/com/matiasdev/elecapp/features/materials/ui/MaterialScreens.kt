package com.matiasdev.elecapp.features.materials.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.materials.data.MaterialRepository
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatusActions
import com.matiasdev.elecapp.features.materials.domain.MaterialUnit
import com.matiasdev.elecapp.features.materials.domain.PurchaseResponsibility
import com.matiasdev.elecapp.features.materials.summary.label
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.quotes.ui.CopyShareButtons
import com.matiasdev.elecapp.features.visits.data.VisitRepository

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaterialListsScreen(
    repository: MaterialRepository,
    onBackClick: () -> Unit,
    onCreateClick: () -> Unit,
    onListClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MaterialListsViewModel = viewModel(factory = MaterialListsViewModelFactory(repository)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier,
        topBar = { MaterialTopBar("Materiales", onBackClick) },
        floatingActionButton = { Button(onClick = onCreateClick) { Icon(Icons.Default.Add, null); Text("Nueva lista") } },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                OutlinedTextField(uiState.query, viewModel::updateQuery, label = { Text("Buscar por cliente, título, material o dirección") }, modifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    listOf(MaterialListStatus.DRAFT, MaterialListStatus.READY, MaterialListStatus.DELIVERED, MaterialListStatus.PURCHASED, null).forEach { status ->
                        FilterChip(uiState.selectedStatus == status, { viewModel.selectStatus(status) }, label = { Text(status?.label() ?: "Todas") })
                    }
                }
            }
            if (uiState.lists.isEmpty()) item { Text("No hay listas de materiales para mostrar") }
            items(uiState.lists, key = { it.materialList.id }) { row ->
                Card(onClick = { onListClick(row.materialList.id) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(row.materialList.title, fontWeight = FontWeight.Bold)
                        Text(row.clientName)
                        Text("${row.materialList.status.label()} · ${row.itemCount} material(es)")
                        Text(row.materialList.purchaseResponsibility.label())
                        row.quoteNumber?.let { Text("Presupuesto $it") }
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialListFormScreen(
    repository: MaterialRepository,
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    inspectionRepository: InspectionRepository,
    quoteRepository: QuoteRepository,
    listId: String?,
    clientId: String?,
    visitId: String?,
    inspectionId: String?,
    quoteId: String?,
    onBackClick: () -> Unit,
    onSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MaterialListFormViewModel = viewModel(
        factory = MaterialListFormViewModelFactory(
            repository,
            clientRepository,
            visitRepository,
            inspectionRepository,
            quoteRepository,
            listId,
            clientId,
            visitId,
            inspectionId,
            quoteId,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.savedListId) {
        uiState.savedListId?.let {
            onSaved(it)
            viewModel.clearSavedEvent()
        }
    }
    Scaffold(modifier = modifier, topBar = { MaterialTopBar("Lista de materiales", onBackClick) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { MaterialClientSelector(uiState, viewModel) }
            item { MaterialListFields(uiState, viewModel) }
            item { MaterialTemplateButtons(viewModel) }
            items(uiState.items, key = { it.id }) { item -> MaterialItemEditor(item, viewModel) }
            item {
                uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { viewModel.save(MaterialSaveMode.DRAFT) }, modifier = Modifier.fillMaxWidth()) { Text("Guardar borrador") }
                    Button(onClick = { viewModel.save(MaterialSaveMode.READY) }, modifier = Modifier.fillMaxWidth()) { Text("Marcar lista preparada") }
                }
            }
        }
    }
}

@Composable
fun MaterialListDetailScreen(
    repository: MaterialRepository,
    clientRepository: ClientRepository,
    quoteRepository: QuoteRepository,
    listId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onCancelled: () -> Unit,
    onMaterialsListClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MaterialListDetailViewModel = viewModel(
        factory = MaterialListDetailViewModelFactory(repository, clientRepository, quoteRepository, listId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                MaterialListDetailEvent.Cancelled -> {
                    snackbarHostState.showSnackbar("Lista cancelada")
                    onCancelled()
                }
                is MaterialListDetailEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { MaterialTopBar("Detalle de materiales", onBackClick) },
    ) { padding ->
        MaterialListDetailContent(
            uiState = uiState,
            onIncludePricesChange = viewModel::setIncludePrices,
            onEditClick = { onEditClick(listId) },
            onPrimaryTransition = viewModel::applyPrimaryTransition,
            onCancel = viewModel::cancelList,
            onCopy = { clipboard.setText(AnnotatedString(uiState.shareText)) },
            onShare = { shareMaterialText(context, uiState.shareText) },
            onMaterialsListClick = onMaterialsListClick,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun MaterialListDetailContent(
    uiState: MaterialListDetailUiState,
    onIncludePricesChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onPrimaryTransition: () -> Unit,
    onCancel: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onMaterialsListClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val list = uiState.list
    val showCancelDialog = remember { mutableStateOf(false) }
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            if (list == null) return@item Text("Lista no encontrada")
            MaterialListHeader(uiState)
        }
        items(uiState.items, key = { it.id }) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.description, fontWeight = FontWeight.SemiBold)
                    Text("${item.quantity} ${item.unit.label(item.customUnitLabel)}")
                    item.specifications?.takeIf(String::isNotBlank)?.let { Text(it) }
                    item.preferredBrand?.takeIf(String::isNotBlank)?.let { Text("Marca sugerida: $it") }
                }
            }
        }
        if (list != null) {
            item {
                MaterialListActions(
                    uiState = uiState,
                    onIncludePricesChange = onIncludePricesChange,
                    onEditClick = onEditClick,
                    onPrimaryTransition = onPrimaryTransition,
                    onCopy = onCopy,
                    onShare = onShare,
                    onMaterialsListClick = onMaterialsListClick,
                    onCancelRequest = { showCancelDialog.value = true },
                )
            }
        }
    }
    if (showCancelDialog.value) {
        AlertDialog(
            onDismissRequest = { showCancelDialog.value = false },
            title = { Text("Cancelar lista") },
            text = { Text("¿Querés cancelar esta lista de materiales?") },
            confirmButton = { TextButton(onClick = { showCancelDialog.value = false; onCancel() }) { Text("Cancelar lista") } },
            dismissButton = { TextButton(onClick = { showCancelDialog.value = false }) { Text("Seguir editando") } },
        )
    }
}

@Composable
private fun MaterialListHeader(uiState: MaterialListDetailUiState) {
    val list = uiState.list ?: return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(list.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(uiState.client?.fullName.orEmpty())
            Text("Estado: ${list.status.label()}")
            Text("Compra: ${list.purchaseResponsibility.label()}")
            list.visitId?.let { Text("Visita vinculada") }
            uiState.quote?.quoteNumber?.let { Text("Presupuesto $it") }
        }
    }
}

@Composable
private fun MaterialListActions(
    uiState: MaterialListDetailUiState,
    onIncludePricesChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onPrimaryTransition: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onMaterialsListClick: () -> Unit,
    onCancelRequest: () -> Unit,
) {
    val list = uiState.list ?: return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        if (uiState.items.any { it.estimatedUnitPriceAmount != null || it.actualUnitPriceAmount != null }) {
            Row { Checkbox(uiState.includePrices, onIncludePricesChange); Text("Incluir precios al compartir") }
        }
        list.status.primaryActionLabel()?.let { label ->
            Button(onClick = onPrimaryTransition, modifier = Modifier.fillMaxWidth()) { Text(label) }
        }
        Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth()) { Text("Editar") }
        CopyShareButtons(uiState.shareText, "Copiar lista", "Compartir lista", onCopy, onShare)
        if (list.status == MaterialListStatus.PURCHASED) {
            OutlinedButton(onClick = onMaterialsListClick, modifier = Modifier.fillMaxWidth()) { Text("Volver a materiales") }
        }
        if (MaterialListStatusActions.canCancel(list.status)) {
            OutlinedButton(onClick = onCancelRequest, modifier = Modifier.fillMaxWidth()) { Text("Cancelar lista") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialTopBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
    )
}

private fun shareMaterialText(context: android.content.Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir lista"))
}
