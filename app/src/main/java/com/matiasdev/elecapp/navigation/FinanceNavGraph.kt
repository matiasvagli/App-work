package com.matiasdev.elecapp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.finance.domain.AttentionReportCoordinator
import com.matiasdev.elecapp.features.finance.ui.FinanceDashboardScreen
import com.matiasdev.elecapp.features.finance.ui.QuickVisitScreen
import com.matiasdev.elecapp.features.finance.ui.RegisterPaymentScreen
import com.matiasdev.elecapp.features.finance.ui.ServiceReceiptDetailScreen
import com.matiasdev.elecapp.features.finance.ui.ServiceReceiptListScreen
import com.matiasdev.elecapp.features.finance.ui.VisitCloseScreen
import com.matiasdev.elecapp.features.finance.ui.VisitWorkScreen
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionRepository

fun NavGraphBuilder.financeRoutes(
    navController: NavHostController,
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    workSessionRepository: VisitWorkSessionRepository,
    financeRepository: FinanceRepository,
    inspectionRepository: InspectionRepository,
    attentionReportCoordinator: AttentionReportCoordinator,
) {
    composable(AppRoutes.QUICK_VISIT) {
        QuickVisitScreen(
            clientRepository = clientRepository,
            financeRepository = financeRepository,
            onBackClick = { navController.navigateUp() },
            onVisitStarted = { visitId ->
                navController.navigate(AppRoutes.visitDetail(visitId)) {
                    popUpTo(AppRoutes.QUICK_VISIT) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onContinueCurrentVisit = {
                navController.popBackStack(AppRoutes.HOME, inclusive = false)
            },
        )
    }
    composable(AppRoutes.VISIT_CLOSE, AppRoutes.visitCloseArguments) { backStackEntry ->
        val visitId = backStackEntry.arguments?.getString(AppRoutes.VISIT_ID).orEmpty()
        VisitCloseScreen(
            clientRepository = clientRepository,
            visitRepository = visitRepository,
            workSessionRepository = workSessionRepository,
            financeRepository = financeRepository,
            attentionReportCoordinator = attentionReportCoordinator,
            visitId = visitId,
            onBackClick = { navController.navigateUp() },
            onSaved = { savedVisitId, receiptId ->
                navController.popBackStack(AppRoutes.visitDetail(savedVisitId), inclusive = false)
                if (receiptId != null) navController.navigate(AppRoutes.serviceReceiptDetail(receiptId)) { launchSingleTop = true }
            },
        )
    }
    composable(AppRoutes.VISIT_WORK, AppRoutes.visitIdArguments) { backStackEntry ->
        VisitWorkScreen(
            clientRepository = clientRepository,
            visitRepository = visitRepository,
            workSessionRepository = workSessionRepository,
            financeRepository = financeRepository,
            visitId = backStackEntry.arguments?.getString(AppRoutes.VISIT_ID).orEmpty(),
            onBackClick = { navController.navigateUp() },
            onSaved = { navController.navigateUp() },
        )
    }
    composable(AppRoutes.FINANCE_DASHBOARD) {
        FinanceDashboardScreen(financeRepository = financeRepository, onBackClick = { navController.navigateUp() })
    }
    composable(AppRoutes.SERVICE_RECEIPTS, AppRoutes.serviceReceiptArguments) { backStackEntry ->
        ServiceReceiptListScreen(
            financeRepository = financeRepository,
            clientId = backStackEntry.arguments?.getString(AppRoutes.CLIENT_ID)?.takeIf { it.isNotBlank() },
            onBackClick = { navController.navigateUp() },
            onReceiptClick = { navController.navigate(AppRoutes.serviceReceiptDetail(it)) { launchSingleTop = true } },
        )
    }
    composable(AppRoutes.SERVICE_RECEIPT_DETAIL, AppRoutes.receiptIdArguments) { backStackEntry ->
        ServiceReceiptDetailScreen(
            financeRepository = financeRepository,
            clientRepository = clientRepository,
            inspectionRepository = inspectionRepository,
            receiptId = backStackEntry.arguments?.getString(AppRoutes.RECEIPT_ID).orEmpty(),
            onBackClick = { navController.navigateUp() },
            onHomeClick = { navController.navigateHome() },
            onVisitClick = { navController.navigate(AppRoutes.visitDetail(it)) { launchSingleTop = true } },
            onFullReportClick = { navController.navigate(AppRoutes.inspectionOverview(it)) { launchSingleTop = true } },
            onRegisterPaymentClick = { receiptId, clientId, visitId ->
                navController.navigate(AppRoutes.registerPayment(receiptId, clientId, visitId))
            },
        )
    }
    composable(AppRoutes.REGISTER_PAYMENT, AppRoutes.registerPaymentArguments) { backStackEntry ->
        RegisterPaymentScreen(
            financeRepository = financeRepository,
            receiptId = backStackEntry.arguments?.getString(AppRoutes.RECEIPT_ID)?.takeIf { it.isNotBlank() },
            clientId = backStackEntry.arguments?.getString(AppRoutes.CLIENT_ID).orEmpty(),
            visitId = backStackEntry.arguments?.getString(AppRoutes.VISIT_ID)?.takeIf { it.isNotBlank() },
            onBackClick = { navController.navigateUp() },
            onSaved = { navController.navigateUp() },
        )
    }
}

private fun NavHostController.navigateHome() {
    navigate(AppRoutes.HOME) {
        popUpTo(AppRoutes.HOME) { inclusive = false }
        launchSingleTop = true
    }
}
