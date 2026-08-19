package com.matiasdev.elecapp.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfigRepository
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.electricaltools.ui.ElectricalReferenceScreen
import com.matiasdev.elecapp.features.electricaltools.ui.ElectricalToolsHistoryScreen
import com.matiasdev.elecapp.features.electricaltools.ui.ElectricalToolsHomeScreen
import com.matiasdev.elecapp.features.electricaltools.ui.PowerCurrentVoltageScreen
import com.matiasdev.elecapp.features.electricaltools.ui.ReferenceTool
import com.matiasdev.elecapp.features.electricaltools.ui.TechnicalCalculationDetailScreen
import com.matiasdev.elecapp.features.electricaltools.ui.VoltageDropScreen
import com.matiasdev.elecapp.features.electricaltools.ui.diagrams.ElectricalDiagramDetailScreen
import com.matiasdev.elecapp.features.electricaltools.ui.diagrams.ElectricalDiagramsListScreen
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.referencedocs.data.ReferenceDocumentRepository
import com.matiasdev.elecapp.features.referencedocs.data.ReferenceDocumentStorage
import com.matiasdev.elecapp.features.referencedocs.ui.ReferenceDocsScreen
import com.matiasdev.elecapp.features.visits.data.VisitRepository

fun NavGraphBuilder.electricalToolsRoutes(
    navController: NavHostController,
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    inspectionRepository: InspectionRepository,
    technicalCalculationRepository: TechnicalCalculationRepository,
    electricalRuleConfigRepository: ElectricalRuleConfigRepository,
    referenceDocumentRepository: ReferenceDocumentRepository,
    referenceDocumentStorage: ReferenceDocumentStorage,
) {
    composable(AppRoutes.ELECTRICAL_TOOLS) {
        ElectricalToolsHomeScreen(
            onBackClick = { navController.navigateUp() },
            onPowerClick = { navController.navigateSingleTop(AppRoutes.electricalToolsPower()) },
            onVoltageDropClick = { navController.navigateSingleTop(AppRoutes.electricalToolsVoltageDrop()) },
            onHistoryClick = { navController.navigateSingleTop(AppRoutes.ELECTRICAL_TOOLS_HISTORY) },
            onDiagramsClick = { navController.navigateSingleTop(AppRoutes.electricalDiagrams()) },
            onDocumentsClick = { navController.navigateSingleTop(AppRoutes.electricalToolsDocuments()) },
            onReferenceClick = { tool: ReferenceTool -> navController.navigateSingleTop(AppRoutes.electricalToolsReference(tool.name)) },
        )
    }
    composable(AppRoutes.ELECTRICAL_TOOLS_REFERENCE) { entry ->
        val tool = runCatching { ReferenceTool.valueOf(entry.arguments?.getString("tool").orEmpty()) }
            .getOrDefault(ReferenceTool.TABLES)
        ElectricalReferenceScreen(tool = tool, onBackClick = { navController.navigateUp() })
    }
    composable(AppRoutes.ELECTRICAL_TOOLS_DOCUMENTS) {
        ReferenceDocsScreen(
            repository = referenceDocumentRepository,
            storage = referenceDocumentStorage,
            onBackClick = { navController.navigateUp() },
        )
    }
    composable(AppRoutes.ELECTRICAL_DIAGRAMS) {
        ElectricalDiagramsListScreen(
            onBackClick = { navController.navigateUp() },
            onDiagramClick = { diagramId ->
                navController.navigateSingleTop(AppRoutes.electricalDiagramDetail(diagramId))
            },
        )
    }
    composable(AppRoutes.ELECTRICAL_DIAGRAM_DETAIL, AppRoutes.diagramIdArguments) { backStackEntry ->
        val diagramId = backStackEntry.arguments?.getString(AppRoutes.DIAGRAM_ID).orEmpty()
        ElectricalDiagramDetailScreen(
            diagramId = diagramId,
            onBackClick = { navController.navigateUp() },
        )
    }
    composable(AppRoutes.ELECTRICAL_TOOLS_HISTORY) {
        ElectricalToolsHistoryScreen(
            repository = technicalCalculationRepository,
            onBackClick = { navController.navigateUp() },
            onDetailClick = { navController.navigateSingleTop(AppRoutes.technicalCalculationDetail(it)) },
            onDuplicatePower = { navController.navigateSingleTop(AppRoutes.electricalToolsPower(duplicateId = it)) },
            onDuplicateVoltageDrop = { navController.navigateSingleTop(AppRoutes.electricalToolsVoltageDrop(duplicateId = it)) },
        )
    }
    composable(AppRoutes.ELECTRICAL_TOOLS_POWER, AppRoutes.electricalToolArguments) { backStackEntry ->
        PowerCurrentVoltageScreen(
            repository = technicalCalculationRepository,
            clientRepository = clientRepository,
            visitRepository = visitRepository,
            inspectionRepository = inspectionRepository,
            electricalRuleConfigRepository = electricalRuleConfigRepository,
            clientId = backStackEntry.optionalArg(AppRoutes.CLIENT_ID),
            visitId = backStackEntry.optionalArg(AppRoutes.VISIT_ID),
            inspectionId = backStackEntry.optionalArg(AppRoutes.INSPECTION_ID),
            duplicateId = backStackEntry.optionalArg(AppRoutes.DUPLICATE_ID),
            onBackClick = { navController.navigateUp() },
        )
    }
    composable(AppRoutes.ELECTRICAL_TOOLS_VOLTAGE_DROP, AppRoutes.electricalToolArguments) { backStackEntry ->
        VoltageDropScreen(
            repository = technicalCalculationRepository,
            clientRepository = clientRepository,
            visitRepository = visitRepository,
            inspectionRepository = inspectionRepository,
            clientId = backStackEntry.optionalArg(AppRoutes.CLIENT_ID),
            visitId = backStackEntry.optionalArg(AppRoutes.VISIT_ID),
            inspectionId = backStackEntry.optionalArg(AppRoutes.INSPECTION_ID),
            duplicateId = backStackEntry.optionalArg(AppRoutes.DUPLICATE_ID),
            onBackClick = { navController.navigateUp() },
        )
    }
    composable(AppRoutes.TECHNICAL_CALCULATION_DETAIL, AppRoutes.technicalCalculationIdArguments) { backStackEntry ->
        TechnicalCalculationDetailScreen(
            repository = technicalCalculationRepository,
            clientRepository = clientRepository,
            inspectionRepository = inspectionRepository,
            calculationId = backStackEntry.arguments?.getString(AppRoutes.CALCULATION_ID).orEmpty(),
            onBackClick = { navController.navigateUp() },
            onDeleted = { navController.navigateUp() },
        )
    }
}

private fun NavBackStackEntry.optionalArg(name: String): String? {
    return arguments?.getString(name)?.takeIf { it.isNotBlank() }
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) { launchSingleTop = true }
}
