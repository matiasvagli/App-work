package com.matiasdev.elecapp.features.materials.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.matiasdev.elecapp.features.materials.domain.MaterialUnit
import com.matiasdev.elecapp.features.materials.domain.PurchaseResponsibility
import com.matiasdev.elecapp.features.materials.summary.label
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.quotes.ui.CopyShareButtons
import com.matiasdev.elecapp.features.visits.data.VisitRepository

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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
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
    LaunchedEffect(uiState.savedListId) { uiState.savedListId?.let(onSaved) }
    Scaffold(modifier = modifier, topBar = { MaterialTopBar("Lista de materiales", onBackClick) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { MaterialClientSelector(uiState, viewModel) }
            item { MaterialListFields(uiState, viewModel) }
            item { MaterialTemplateButtons(viewModel) }
            items(uiState.items, key = { it.id }) { item -> MaterialItemEditor(item, viewModel) }
            item {
                uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.save(MaterialSaveMode.DRAFT) }) { Text("Guardar borrador") }
                    Button(onClick = { viewModel.save(MaterialSaveMode.READY) }) { Text("Marcar lista") }
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
    modifier: Modifier = Modifier,
    viewModel: MaterialListDetailViewModel = viewModel(
        factory = MaterialListDetailViewModelFactory(repository, clientRepository, quoteRepository, listId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    Scaffold(modifier = modifier, topBar = { MaterialTopBar("Detalle de materiales", onBackClick) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                val list = uiState.list ?: return@item Text("Lista no encontrada")
                Text(list.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(uiState.client?.fullName.orEmpty())
                Text(list.status.label())
                Text(list.purchaseResponsibility.label())
            }
            items(uiState.items, key = { it.id }) { item ->
                Card { Column(Modifier.padding(12.dp)) {
                    Text(item.description, fontWeight = FontWeight.SemiBold)
                    Text("${item.quantity} ${item.unit.label(item.customUnitLabel)}")
                    item.specifications?.let { Text(it) }
                } }
            }
            item {
                Row { Checkbox(uiState.includePrices, viewModel::setIncludePrices); Text("Incluir precios en el texto") }
                Button(onClick = { onEditClick(listId) }) { Text("Editar") }
                CopyShareButtons(
                    text = uiState.shareText,
                    copyLabel = "Copiar lista",
                    shareTitle = "Compartir lista",
                    onCopy = { clipboard.setText(AnnotatedString(uiState.shareText)) },
                    onShare = { shareMaterialText(context, uiState.shareText) },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(MaterialListStatus.READY, MaterialListStatus.DELIVERED, MaterialListStatus.PURCHASED, MaterialListStatus.CANCELLED).forEach { status ->
                        FilterChip(uiState.list?.status == status, { viewModel.changeStatus(status) }, label = { Text(status.label()) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialTopBar(title: String, onBackClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}

private fun shareMaterialText(context: android.content.Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir lista"))
}
