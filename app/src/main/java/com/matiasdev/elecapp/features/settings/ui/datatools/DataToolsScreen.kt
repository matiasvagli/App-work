package com.matiasdev.elecapp.features.settings.ui.datatools

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.external.shareTextIntent
import com.matiasdev.elecapp.core.external.tryStartActivity
import com.matiasdev.elecapp.core.ui.theme.ElecAppTheme
import com.matiasdev.elecapp.features.settings.data.AppDataReset
import com.matiasdev.elecapp.features.settings.data.DemoDataSeeder
import com.matiasdev.elecapp.features.settings.domain.FeedbackContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataToolsScreen(
    seeder: DemoDataSeeder,
    reset: AppDataReset,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DataToolsViewModel = viewModel(factory = DataToolsViewModelFactory(seeder, reset))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val feedbackContext = remember(context) { context.feedbackContext() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DataToolsEvent.ShareText -> {
                    val shared = context.tryStartActivity(shareTextIntent(event.text, "Enviar comentario"))
                    if (!shared) snackbarHostState.showSnackbar("No se pudo abrir el menú de compartir")
                }

                is DataToolsEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Datos y pruebas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        DataToolsContent(
            uiState = uiState,
            onCommentChange = viewModel::onCommentChange,
            onSendFeedback = { viewModel.onSendFeedback(feedbackContext) },
            onSeedDemoData = viewModel::onSeedDemoData,
            onWipeAll = viewModel::onWipeAll,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun DataToolsContent(
    uiState: DataToolsUiState,
    onCommentChange: (String) -> Unit,
    onSendFeedback: () -> Unit,
    onSeedDemoData: () -> Unit,
    onWipeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmingWipe by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionCard(
            title = "Datos de ejemplo",
            description = "Carga clientes, visitas, un presupuesto y comprobantes con saldo " +
                "pendiente, para recorrer la app sin tener que inventar todo a mano. Se suma a " +
                "lo que ya haya cargado.",
        ) {
            OutlinedButton(
                onClick = onSeedDemoData,
                enabled = !uiState.isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isSeeding) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Cargar datos de ejemplo")
                }
            }
        }

        SectionCard(
            title = "Enviar un comentario",
            description = "Contá qué encontraste: qué esperabas, qué pasó y en qué pantalla. " +
                "Se adjuntan versión de la app, de Android y modelo del equipo.",
        ) {
            OutlinedTextField(
                value = uiState.comment,
                onValueChange = onCommentChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Comentario") },
                minLines = 4,
            )
            Button(
                onClick = onSendFeedback,
                enabled = uiState.canSendFeedback,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("  Enviar comentario")
            }
        }

        SectionCard(
            title = "Borrar todos los datos",
            description = "Deja la app como recién instalada. Se borran clientes, visitas, " +
                "relevamientos, presupuestos, comprobantes, cobros y los PDF importados. " +
                "No se puede deshacer.",
        ) {
            Button(
                onClick = { confirmingWipe = true },
                enabled = !uiState.isBusy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isWiping) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Borrar todos los datos")
                }
            }
        }
    }

    if (confirmingWipe) {
        AlertDialog(
            onDismissRequest = { confirmingWipe = false },
            title = { Text("¿Borrar todos los datos?") },
            text = {
                Text(
                    "Se van a borrar todos los clientes, visitas, relevamientos, presupuestos, " +
                        "comprobantes, cobros y los PDF importados.\n\n" +
                        "Esta acción no se puede deshacer y la app no tiene copia de seguridad.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingWipe = false
                        onWipeAll()
                    },
                ) {
                    Text("Sí, borrar todo", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingWipe = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}

/**
 * La versión sale del `PackageManager` y no de `BuildConfig`, que este módulo no genera.
 */
private fun Context.feedbackContext(): FeedbackContext {
    val version = runCatching { packageManager.getPackageInfo(packageName, 0).versionName }
        .getOrNull()
        ?: "desconocida"
    return FeedbackContext(
        appVersion = version,
        androidRelease = Build.VERSION.RELEASE ?: "desconocida",
        androidSdk = Build.VERSION.SDK_INT,
        deviceModel = listOf(Build.MANUFACTURER, Build.MODEL).joinToString(" ").trim(),
    )
}

@Preview(showBackground = true)
@Composable
private fun DataToolsPreview() {
    ElecAppTheme {
        DataToolsContent(
            uiState = DataToolsUiState(comment = "Al cerrar la visita no me tomó el monto."),
            onCommentChange = {},
            onSendFeedback = {},
            onSeedDemoData = {},
            onWipeAll = {},
        )
    }
}
