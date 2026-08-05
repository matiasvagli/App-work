package com.matiasdev.elecapp.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.materials.data.MaterialRepository
import com.matiasdev.elecapp.features.materials.ui.MaterialListDetailScreen
import com.matiasdev.elecapp.features.materials.ui.MaterialListFormScreen
import com.matiasdev.elecapp.features.materials.ui.MaterialListsScreen
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.quotes.ui.QuoteDetailScreen
import com.matiasdev.elecapp.features.quotes.ui.QuoteFormScreen
import com.matiasdev.elecapp.features.quotes.ui.QuotesListScreen
import com.matiasdev.elecapp.features.visits.data.VisitRepository

fun NavGraphBuilder.documentRoutes(
    navController: NavHostController,
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    inspectionRepository: InspectionRepository,
    quoteRepository: QuoteRepository,
    materialRepository: MaterialRepository,
) {
    composable(AppRoutes.QUOTES) {
        QuotesListScreen(
            repository = quoteRepository,
            onBackClick = { navController.navigateUp() },
            onCreateClick = { navController.navigate(AppRoutes.quoteCreate()) },
            onQuoteClick = { navController.navigateSingleTop(AppRoutes.quoteDetail(it)) },
        )
    }
    composable(AppRoutes.MATERIALS) {
        MaterialListsScreen(
            repository = materialRepository,
            onBackClick = { navController.navigateUp() },
            onCreateClick = { navController.navigate(AppRoutes.materialCreate()) },
            onListClick = { navController.navigateSingleTop(AppRoutes.materialDetail(it)) },
        )
    }
    composable(AppRoutes.QUOTE_CREATE, AppRoutes.quoteCreateArguments) { backStackEntry ->
        QuoteFormScreen(
            quoteRepository = quoteRepository,
            clientRepository = clientRepository,
            visitRepository = visitRepository,
            inspectionRepository = inspectionRepository,
            quoteId = null,
            clientId = backStackEntry.optionalArg(AppRoutes.CLIENT_ID),
            visitId = backStackEntry.optionalArg(AppRoutes.VISIT_ID),
            inspectionId = backStackEntry.optionalArg(AppRoutes.INSPECTION_ID),
            onBackClick = { navController.navigateUp() },
            onSaved = { navController.navigateReplacingCurrent(backStackEntry, AppRoutes.quoteDetail(it)) },
        )
    }
    composable(AppRoutes.QUOTE_DETAIL, AppRoutes.quoteIdArguments) { backStackEntry ->
        val quoteId = backStackEntry.arguments?.getString(AppRoutes.QUOTE_ID).orEmpty()
        QuoteDetailScreen(
            quoteRepository = quoteRepository,
            clientRepository = clientRepository,
            materialRepository = materialRepository,
            quoteId = quoteId,
            onBackClick = { navController.navigateUp() },
            onEditClick = { navController.navigate(AppRoutes.quoteEdit(it)) },
            onCreateMaterialClick = { navController.navigate(AppRoutes.materialCreate(quoteId = it)) },
            onCancelled = { navController.navigateQuotesList() },
        )
    }
    composable(AppRoutes.QUOTE_EDIT, AppRoutes.quoteIdArguments) { backStackEntry ->
        val quoteId = backStackEntry.arguments?.getString(AppRoutes.QUOTE_ID).orEmpty()
        QuoteFormScreen(
            quoteRepository = quoteRepository,
            clientRepository = clientRepository,
            visitRepository = visitRepository,
            inspectionRepository = inspectionRepository,
            quoteId = quoteId,
            clientId = null,
            visitId = null,
            inspectionId = null,
            onBackClick = { navController.navigateUp() },
            onSaved = { navController.navigateReplacingCurrent(backStackEntry, AppRoutes.quoteDetail(it)) },
        )
    }
    composable(AppRoutes.MATERIAL_CREATE, AppRoutes.materialCreateArguments) { backStackEntry ->
        MaterialListFormScreen(
            repository = materialRepository,
            clientRepository = clientRepository,
            visitRepository = visitRepository,
            inspectionRepository = inspectionRepository,
            quoteRepository = quoteRepository,
            listId = null,
            clientId = backStackEntry.optionalArg(AppRoutes.CLIENT_ID),
            visitId = backStackEntry.optionalArg(AppRoutes.VISIT_ID),
            inspectionId = backStackEntry.optionalArg(AppRoutes.INSPECTION_ID),
            quoteId = backStackEntry.optionalArg(AppRoutes.QUOTE_ID),
            onBackClick = { navController.navigateUp() },
            onSaved = { navController.navigateReplacingCurrent(backStackEntry, AppRoutes.materialDetail(it)) },
        )
    }
    composable(AppRoutes.MATERIAL_DETAIL, AppRoutes.materialListIdArguments) { backStackEntry ->
        val listId = backStackEntry.arguments?.getString(AppRoutes.MATERIAL_LIST_ID).orEmpty()
        MaterialListDetailScreen(
            repository = materialRepository,
            clientRepository = clientRepository,
            quoteRepository = quoteRepository,
            listId = listId,
            onBackClick = { navController.navigateUp() },
            onEditClick = { navController.navigate(AppRoutes.materialEdit(it)) },
            onCancelled = { navController.navigateMaterialsList() },
            onMaterialsListClick = { navController.navigateMaterialsList() },
        )
    }
    composable(AppRoutes.MATERIAL_EDIT, AppRoutes.materialListIdArguments) { backStackEntry ->
        val listId = backStackEntry.arguments?.getString(AppRoutes.MATERIAL_LIST_ID).orEmpty()
        MaterialListFormScreen(
            repository = materialRepository,
            clientRepository = clientRepository,
            visitRepository = visitRepository,
            inspectionRepository = inspectionRepository,
            quoteRepository = quoteRepository,
            listId = listId,
            clientId = null,
            visitId = null,
            inspectionId = null,
            quoteId = null,
            onBackClick = { navController.navigateUp() },
            onSaved = { navController.navigateReplacingCurrent(backStackEntry, AppRoutes.materialDetail(it)) },
        )
    }
}

private fun NavBackStackEntry.optionalArg(name: String): String? {
    return arguments?.getString(name)?.takeIf { it.isNotBlank() }
}

private fun NavHostController.navigateReplacingCurrent(backStackEntry: NavBackStackEntry, route: String) {
    navigate(route) {
        popUpTo(backStackEntry.destination.id) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) { launchSingleTop = true }
}

private fun NavHostController.navigateMaterialsList() {
    navigate(AppRoutes.MATERIALS) {
        popUpTo(AppRoutes.HOME)
        launchSingleTop = true
    }
}

private fun NavHostController.navigateQuotesList() {
    navigate(AppRoutes.QUOTES) {
        popUpTo(AppRoutes.HOME)
        launchSingleTop = true
    }
}
