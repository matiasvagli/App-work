package com.matiasdev.elecapp.features.materials.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.features.materials.domain.MaterialUnit
import com.matiasdev.elecapp.features.materials.domain.PurchaseResponsibility
import com.matiasdev.elecapp.features.materials.summary.label

@Composable
fun MaterialClientSelector(uiState: MaterialListFormUiState, viewModel: MaterialListFormViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Cliente", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(uiState.clientQuery, viewModel::updateClientQuery, label = { Text("Buscar cliente") }, modifier = Modifier.fillMaxWidth())
            uiState.clients.forEach { client ->
                FilterChip(uiState.clientId == client.id, { viewModel.selectClient(client.id) }, label = { Text(client.fullName) })
            }
        }
    }
}

@Composable
fun MaterialListFields(uiState: MaterialListFormUiState, viewModel: MaterialListFormViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Datos de la lista", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(uiState.title, viewModel::updateTitle, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PurchaseResponsibility.entries.forEach { responsibility ->
                    FilterChip(
                        selected = uiState.purchaseResponsibility == responsibility,
                        onClick = { viewModel.updateResponsibility(responsibility) },
                        label = { Text(responsibility.label()) },
                    )
                }
            }
            OutlinedTextField(uiState.introduction, viewModel::updateIntroduction, label = { Text("Introducción") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            OutlinedTextField(uiState.notes, viewModel::updateNotes, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        }
    }
}

@Composable
fun MaterialTemplateButtons(viewModel: MaterialListFormViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Plantillas rápidas", fontWeight = FontWeight.SemiBold)
        MaterialTemplates.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { template ->
                    OutlinedButton(onClick = { viewModel.addItem(template) }) { Text(template.title) }
                }
            }
        }
        OutlinedButton(onClick = { viewModel.addItem() }) { Text("Agregar material") }
    }
}

@Composable
fun MaterialItemEditor(item: MaterialItemFormState, viewModel: MaterialListFormViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(item.description, { value -> viewModel.updateItem(item.id) { it.copy(description = value) } }, label = { Text("Material") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(item.quantity, { value -> viewModel.updateItem(item.id) { it.copy(quantity = value) } }, label = { Text("Cantidad") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(MaterialUnit.UNIT, MaterialUnit.METER, MaterialUnit.ROLL, MaterialUnit.BOX, MaterialUnit.PACK).forEach { unit ->
                    FilterChip(item.unit == unit, { viewModel.updateItem(item.id) { it.copy(unit = unit) } }, label = { Text(unit.label()) })
                }
            }
            OutlinedTextField(item.specifications, { value -> viewModel.updateItem(item.id) { it.copy(specifications = value) } }, label = { Text("Especificaciones") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(item.preferredBrand, { value -> viewModel.updateItem(item.id) { it.copy(preferredBrand = value) } }, label = { Text("Marca sugerida") }, modifier = Modifier.fillMaxWidth())
            Row {
                Checkbox(item.alternativeAllowed, { value -> viewModel.updateItem(item.id) { it.copy(alternativeAllowed = value) } })
                Text("Permitir alternativas equivalentes")
            }
            Row {
                Checkbox(item.includePrices, { value -> viewModel.updateItem(item.id) { it.copy(includePrices = value) } })
                Text("Registrar precio opcional")
            }
            if (item.includePrices) {
                OutlinedTextField(item.estimatedUnitPriceInput, { value -> viewModel.updateItem(item.id) { it.copy(estimatedUnitPriceInput = value) } }, label = { Text("Precio estimado") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(item.actualUnitPriceInput, { value -> viewModel.updateItem(item.id) { it.copy(actualUnitPriceInput = value) } }, label = { Text("Precio real") }, modifier = Modifier.fillMaxWidth())
            }
            OutlinedTextField(item.notes, { value -> viewModel.updateItem(item.id) { it.copy(notes = value) } }, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton({ viewModel.moveItem(item.id, -1) }) { Text("Subir") }
                TextButton({ viewModel.moveItem(item.id, 1) }) { Text("Bajar") }
                TextButton({ viewModel.duplicateItem(item.id) }) { Text("Duplicar") }
                TextButton({ viewModel.removeItem(item.id) }) { Text("Eliminar") }
            }
        }
    }
}
