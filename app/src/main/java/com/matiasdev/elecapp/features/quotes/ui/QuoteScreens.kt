package com.matiasdev.elecapp.features.quotes.ui

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.quotes.domain.DiscountType
import com.matiasdev.elecapp.features.quotes.domain.MoneyFormatter
import com.matiasdev.elecapp.features.quotes.domain.QuoteCurrency
import com.matiasdev.elecapp.features.quotes.domain.QuoteItemType
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatusActions
import com.matiasdev.elecapp.features.quotes.summary.label
import com.matiasdev.elecapp.features.visits.data.VisitRepository

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuotesListScreen(
    repository: QuoteRepository,
    onBackClick: () -> Unit,
    onCreateClick: () -> Unit,
    onQuoteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuotesListViewModel = viewModel(factory = QuotesListViewModelFactory(repository)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier,
        topBar = { SimpleTopBar("Presupuestos", onBackClick) },
        floatingActionButton = { Button(onClick = onCreateClick) { Icon(Icons.Default.Add, null); Text("Nuevo") } },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::updateQuery,
                    label = { Text("Buscar por cliente, número, título o dirección") },
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    listOf(QuoteStatus.DRAFT, QuoteStatus.READY, QuoteStatus.SENT, QuoteStatus.APPROVED, null).forEach { status ->
                        FilterChip(
                            selected = uiState.selectedStatus == status,
                            onClick = { viewModel.selectStatus(status) },
                            label = { Text(status?.label() ?: "Todos") },
                        )
                    }
                }
            }
            if (uiState.quotes.isEmpty()) {
                item { Text("No hay presupuestos para mostrar") }
            }
            items(uiState.quotes, key = { it.quote.id }) { row ->
                Card(onClick = { onQuoteClick(row.quote.id) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(row.quote.quoteNumber, fontWeight = FontWeight.Bold)
                        Text(row.clientName)
                        Text(row.quote.title)
                        Text("${row.quote.status.label()} · ${MoneyFormatter.format(row.quote.totalAmount, row.quote.currency)}")
                        Text("${row.itemCount} ítem(s)" + if (row.hasMaterialList) " · materiales vinculados" else "")
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteFormScreen(
    quoteRepository: QuoteRepository,
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    inspectionRepository: InspectionRepository,
    quoteId: String?,
    clientId: String?,
    visitId: String?,
    inspectionId: String?,
    onBackClick: () -> Unit,
    onSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuoteFormViewModel = viewModel(
        factory = QuoteFormViewModelFactory(
            quoteRepository,
            clientRepository,
            visitRepository,
            inspectionRepository,
            quoteId,
            clientId,
            visitId,
            inspectionId,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.savedQuoteId) {
        uiState.savedQuoteId?.let {
            onSaved(it)
            viewModel.clearSavedEvent()
        }
    }
    Scaffold(modifier = modifier, topBar = { SimpleTopBar("Presupuesto", onBackClick) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ClientSelector(uiState, viewModel) }
            item { QuoteFields(uiState, viewModel) }
            item { QuoteItemButtons(viewModel) }
            items(uiState.items, key = { it.id }) { item ->
                QuoteItemRow(item, item.id in uiState.expandedItemIds, uiState.currency, viewModel)
            }
            item { QuoteDiscountFields(uiState, viewModel) }
            item {
                uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { viewModel.save(QuoteSaveMode.DRAFT) }, modifier = Modifier.fillMaxWidth()) { Text("Guardar borrador") }
                    Button(onClick = { viewModel.save(QuoteSaveMode.READY) }, modifier = Modifier.fillMaxWidth()) { Text("Marcar listo") }
                }
            }
        }
    }
}

@Composable
fun QuoteDetailScreen(
    quoteRepository: QuoteRepository,
    clientRepository: ClientRepository,
    materialRepository: MaterialRepository,
    quoteId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onCreateMaterialClick: (String) -> Unit,
    onCancelled: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuoteDetailViewModel = viewModel(
        factory = QuoteDetailViewModelFactory(quoteRepository, clientRepository, materialRepository, quoteId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                QuoteDetailEvent.Cancelled -> {
                    snackbarHostState.showSnackbar("Presupuesto cancelado")
                    onCancelled()
                }
                is QuoteDetailEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { SimpleTopBar("Detalle de presupuesto", onBackClick) },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                val quote = uiState.quote ?: return@item Text("Presupuesto no encontrado")
                Text(quote.quoteNumber, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(uiState.client?.fullName.orEmpty())
                Text(quote.title)
                Text("${quote.status.label()} · ${MoneyFormatter.format(quote.totalAmount, quote.currency)}")
            }
            items(uiState.items, key = { it.id }) { item ->
                Card { Column(Modifier.padding(12.dp)) {
                    Text(item.description, fontWeight = FontWeight.SemiBold)
                    Text("${item.quantity} ${item.unit.name.lowercase()} · ${MoneyFormatter.format(item.lineTotalAmount, uiState.quote!!.currency)}")
                } }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Materiales: ${if (uiState.materialList != null) "lista vinculada" else "sin lista vinculada"}")
                    Button(onClick = { onEditClick(quoteId) }) { Text("Editar") }
                    OutlinedButton(onClick = { onCreateMaterialClick(quoteId) }) { Text("Crear lista de materiales") }
                    CopyShareButtons(
                        text = uiState.shareText,
                        copyLabel = "Copiar presupuesto",
                        shareTitle = "Compartir presupuesto",
                        onCopy = { clipboard.setText(AnnotatedString(uiState.shareText)) },
                        onShare = { shareText(context, uiState.shareText, "Compartir presupuesto") },
                    )
                    StatusButtons(uiState.quote?.status, viewModel::changeStatus)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleTopBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
    )
}

private fun shareText(context: android.content.Context, text: String, title: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, title))
}
