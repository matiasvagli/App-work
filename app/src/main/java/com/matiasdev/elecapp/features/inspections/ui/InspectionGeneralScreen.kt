package com.matiasdev.elecapp.features.inspections.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionType
import com.matiasdev.elecapp.features.inspections.domain.PropertyType
import com.matiasdev.elecapp.features.inspections.domain.SupplyType
import com.matiasdev.elecapp.features.inspections.summary.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionGeneralScreen(
    repository: InspectionRepository,
    inspectionId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InspectionGeneralViewModel = viewModel(
        factory = InspectionGeneralViewModelFactory(repository, inspectionId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onBackClick()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Datos generales") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(Modifier.padding(padding).padding(24.dp))
        } else {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                InspectionFormBlock("Características") {
                    InspectionDropdownField("Tipo", uiState.inspectionType, InspectionType.entries.toList(), InspectionType::label, onValueChange = viewModel::onInspectionTypeChange)
                    InspectionDropdownField("Estado general", uiState.generalCondition, GeneralCondition.entries.toList(), GeneralCondition::label, onValueChange = viewModel::onGeneralConditionChange)
                    InspectionDropdownField("Suministro", uiState.supplyType, SupplyType.entries.toList(), SupplyType::label, onValueChange = viewModel::onSupplyTypeChange)
                    InspectionDropdownField("Tipo de propiedad", uiState.propertyType, PropertyType.entries.toList(), PropertyType::label, onValueChange = viewModel::onPropertyTypeChange)
                }
                InspectionFormBlock("Contexto de visita") {
                    InspectionTextField("Técnico", uiState.technicianName, viewModel::onTechnicianNameChange)
                    InspectionTextField("Limitaciones de acceso", uiState.accessLimitations, viewModel::onAccessLimitationsChange, minLines = 3)
                }
                Button(
                    onClick = viewModel::save,
                    enabled = uiState.status == InspectionStatus.DRAFT,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Guardar sección")
                }
            }
        }
    }
}
