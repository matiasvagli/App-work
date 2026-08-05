package com.matiasdev.elecapp.features.visits.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.features.clients.domain.Client

@Composable
fun VisitClientSelector(
    selectedClient: Client?,
    searchQuery: String,
    searchResults: List<Client>,
    isLoading: Boolean,
    onSearchChange: (String) -> Unit,
    onSelectClient: (Client) -> Unit,
    onChangeClient: () -> Unit,
    onCreateClientClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Cliente", style = MaterialTheme.typography.titleMedium)
        if (selectedClient != null) {
            ListItem(
                headlineContent = { Text(selectedClient.fullName) },
                supportingContent = {
                    Text(
                        listOf(selectedClient.phone, selectedClient.locality.orEmpty())
                            .filter(String::isNotBlank)
                            .joinToString(" · "),
                    )
                },
            )
            OutlinedButton(onClick = onChangeClient, modifier = Modifier.fillMaxWidth()) {
                Text("Cambiar cliente")
            }
            return
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar por nombre o teléfono") },
            singleLine = true,
        )
        when {
            isLoading -> CircularProgressIndicator()
            searchResults.isEmpty() -> {
                Text("No hay clientes para mostrar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = onCreateClientClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Crear cliente")
                }
            }
            else -> searchResults.forEach { client ->
                ListItem(
                    headlineContent = { Text(client.fullName) },
                    supportingContent = {
                        Text(
                            listOf(client.phone, client.locality.orEmpty())
                                .filter(String::isNotBlank)
                                .joinToString(" · "),
                        )
                    },
                    modifier = Modifier.clickable { onSelectClient(client) },
                )
            }
        }
    }
}
