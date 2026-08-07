package com.matiasdev.elecapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.matiasdev.elecapp.core.external.SharedClientDraft
import com.matiasdev.elecapp.features.agenda.ui.AgendaScreen
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfigRepository
import com.matiasdev.elecapp.features.electricalrules.ui.ElectricalRulesSettingsScreen
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.clients.ui.ClientDetailScreen
import com.matiasdev.elecapp.features.clients.ui.ClientFormDraft
import com.matiasdev.elecapp.features.clients.ui.ClientFormScreen
import com.matiasdev.elecapp.features.clients.ui.ClientListScreen
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.home.ui.HomeScreen
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionSection
import com.matiasdev.elecapp.features.inspections.ui.FindingsScreen
import com.matiasdev.elecapp.features.inspections.ui.InspectionFinalReportScreen
import com.matiasdev.elecapp.features.inspections.ui.InspectionGeneralScreen
import com.matiasdev.elecapp.features.inspections.ui.InspectionManualCalculationType
import com.matiasdev.elecapp.features.inspections.ui.InspectionOverviewScreen
import com.matiasdev.elecapp.features.inspections.ui.InspectionScopeSelectionScreen
import com.matiasdev.elecapp.features.inspections.ui.InspectionTechnicalCommentScreen
import com.matiasdev.elecapp.features.inspections.ui.InspectionsListScreen
import com.matiasdev.elecapp.features.inspections.ui.MainPanelInspectionScreen
import com.matiasdev.elecapp.features.inspections.ui.PillarInspectionScreen
import com.matiasdev.elecapp.features.inspections.ui.VisualInspectionComplementaryScreen
import com.matiasdev.elecapp.features.inspections.ui.UnverifiedItemsScreen
import com.matiasdev.elecapp.features.materials.data.MaterialRepository
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.reminders.data.VisitReminderRepository
import com.matiasdev.elecapp.features.reminders.scheduling.ReminderCoordinator
import com.matiasdev.elecapp.features.settings.data.ReminderSettingsRepository
import com.matiasdev.elecapp.features.settings.ui.SettingsScreen
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionRepository
import com.matiasdev.elecapp.features.visits.ui.ClientVisitsScreen
import com.matiasdev.elecapp.features.visits.ui.VisitDetailScreen
import com.matiasdev.elecapp.features.visits.ui.VisitFormScreen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun ElecNavHost(
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    workSessionRepository: VisitWorkSessionRepository,
    inspectionRepository: InspectionRepository,
    quoteRepository: QuoteRepository,
    materialRepository: MaterialRepository,
    technicalCalculationRepository: TechnicalCalculationRepository,
    financeRepository: FinanceRepository,
    reminderRepository: VisitReminderRepository,
    settingsRepository: ReminderSettingsRepository,
    electricalRuleConfigRepository: ElectricalRuleConfigRepository,
    reminderCoordinator: ReminderCoordinator,
    initialSharedClientDraft: SharedClientDraft? = null,
    initialVisitId: String? = null,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val sharedClientDraftHandled = remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topLevelRoutes = remember {
        setOf(
            AppRoutes.HOME,
            AppRoutes.AGENDA,
            AppRoutes.CLIENTS,
            AppRoutes.ELECTRICAL_TOOLS,
            AppRoutes.FINANCE_DASHBOARD,
        )
    }
    val showBottomBar = currentRoute in topLevelRoutes

    LaunchedEffect(initialSharedClientDraft) {
        val draft = initialSharedClientDraft
        if (!sharedClientDraftHandled.value && draft != null) {
            sharedClientDraftHandled.value = true
            navController.navigate(
                AppRoutes.clientCreate(
                    fullName = draft.fullName,
                    phone = draft.phones.singleOrNull().orEmpty(),
                    phones = draft.phones.takeIf { it.size > 1 }?.joinToString("\n"),
                    email = draft.email,
                    notes = draft.notes,
                    source = AppRoutes.SOURCE_SHARED_TEXT,
                ),
            )
        }
    }

    LaunchedEffect(initialVisitId) {
        if (!initialVisitId.isNullOrBlank()) {
            navController.navigate(AppRoutes.visitDetail(initialVisitId))
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val items = listOf(
                        Triple(AppRoutes.HOME, "Inicio", Icons.Default.Home),
                        Triple(AppRoutes.AGENDA, "Agenda", Icons.Default.CalendarMonth),
                        Triple(AppRoutes.CLIENTS, "Clientes", Icons.Default.People),
                        Triple(AppRoutes.ELECTRICAL_TOOLS, "Herramientas", Icons.Default.Bolt),
                        Triple(AppRoutes.FINANCE_DASHBOARD, "Economía", Icons.Default.PointOfSale),
                    )
                    items.forEach { (route, label, icon) ->
                        val isSelected = currentRoute == route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(AppRoutes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {

        composable(AppRoutes.HOME) {
            HomeScreen(
                clientRepository = clientRepository,
                visitRepository = visitRepository,
                workSessionRepository = workSessionRepository,
                inspectionRepository = inspectionRepository,
                quoteRepository = quoteRepository,
                materialRepository = materialRepository,
                onElectricalToolsClick = { navController.navigateSingleTop(AppRoutes.ELECTRICAL_TOOLS) },
                onQuickVisitClick = { navController.navigate(AppRoutes.QUICK_VISIT) },
                onFinanceClick = { navController.navigateSingleTop(AppRoutes.FINANCE_DASHBOARD) },
                onClientsClick = { navController.navigateSingleTop(AppRoutes.CLIENTS) },
                onAgendaClick = { navController.navigateSingleTop(AppRoutes.AGENDA) },
                onInspectionsClick = { navController.navigateSingleTop(AppRoutes.INSPECTIONS) },
                onQuotesClick = { navController.navigateSingleTop(AppRoutes.QUOTES) },
                onMaterialsClick = { navController.navigateSingleTop(AppRoutes.MATERIALS) },
                onNewVisitClick = { navController.navigate(AppRoutes.visitCreate()) },
                onVisitClick = { navController.navigateSingleTop(AppRoutes.visitDetail(it)) },
                onSettingsClick = { navController.navigateSingleTop(AppRoutes.SETTINGS) },
            )
        }
        composable(AppRoutes.AGENDA) {
            AgendaScreen(
                clientRepository = clientRepository,
                visitRepository = visitRepository,
                workSessionRepository = workSessionRepository,
                inspectionRepository = inspectionRepository,
                onBackClick = { navController.navigateUp() },
                onCreateVisitClick = { navController.navigate(AppRoutes.visitCreate()) },
                onVisitClick = { navController.navigateSingleTop(AppRoutes.visitDetail(it)) },
            )
        }
        composable(AppRoutes.INSPECTIONS) {
            InspectionsListScreen(
                repository = inspectionRepository,
                onBackClick = { navController.navigateUp() },
                onInspectionClick = { navController.navigateSingleTop(AppRoutes.inspectionOverview(it)) },
            )
        }
        electricalToolsRoutes(
            navController,
            clientRepository,
            visitRepository,
            inspectionRepository,
            technicalCalculationRepository,
            electricalRuleConfigRepository,
        )
        documentRoutes(navController, clientRepository, visitRepository, inspectionRepository, quoteRepository, materialRepository)
        financeRoutes(navController, clientRepository, visitRepository, workSessionRepository, financeRepository)
        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                repository = settingsRepository,
                onBackClick = { navController.navigateUp() },
                onElectricalRulesClick = { navController.navigateSingleTop(AppRoutes.ELECTRICAL_RULES_SETTINGS) },
            )
        }
        composable(AppRoutes.ELECTRICAL_RULES_SETTINGS) {
            ElectricalRulesSettingsScreen(
                repository = electricalRuleConfigRepository,
                onBackClick = { navController.navigateUp() },
            )
        }
        composable(AppRoutes.CLIENTS) {
            ClientListScreen(
                repository = clientRepository,
                onBackClick = { navController.navigateUp() },
                onAddClick = { navController.navigate(AppRoutes.clientCreate()) },
                onClientClick = { clientId -> navController.navigateSingleTop(AppRoutes.clientDetail(clientId)) },
                onEditClick = { clientId -> navController.navigate(AppRoutes.clientEdit(clientId)) },
            )
        }
        composable(
            route = AppRoutes.CLIENT_CREATE,
            arguments = AppRoutes.clientCreateArguments,
        ) { backStackEntry ->
            val fullName = backStackEntry.arguments?.getString(AppRoutes.FULL_NAME)?.takeIf { it.isNotBlank() }
            val notes = backStackEntry.arguments?.getString(AppRoutes.NOTES)?.takeIf { it.isNotBlank() }
            val phone = backStackEntry.arguments?.getString(AppRoutes.PHONE)?.takeIf { it.isNotBlank() }
            val phones = backStackEntry.arguments?.getString(AppRoutes.PHONES)
                ?.takeIf { it.isNotBlank() }
                ?.lineSequence()
                ?.map(String::trim)
                ?.filter(String::isNotBlank)
                ?.toList()
                .orEmpty()
            val email = backStackEntry.arguments?.getString(AppRoutes.EMAIL)?.takeIf { it.isNotBlank() }
            val source = backStackEntry.arguments?.getString(AppRoutes.SOURCE)?.takeIf { it.isNotBlank() }
            val fromVisit = source == AppRoutes.SOURCE_VISIT
            val fromSharedText = source == AppRoutes.SOURCE_SHARED_TEXT
            ClientFormScreen(
                repository = clientRepository,
                clientId = null,
                initialDraft = ClientFormDraft(
                    fullName = fullName.orEmpty(),
                    phone = phone.orEmpty(),
                    phoneChoices = phones,
                    email = email.orEmpty(),
                    notes = notes.orEmpty(),
                ),
                onBackClick = { navController.navigateUp() },
                onSaved = { savedClientId ->
                    if (fromVisit) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(AppRoutes.CREATED_CLIENT_ID, savedClientId)
                    }
                    if (!fromSharedText) navController.navigateUp()
                },
                saveButtonText = if (fromVisit) "Guardar y continuar" else "Guardar",
                showScheduleAfterSave = fromSharedText,
                onScheduleVisitClick = { savedClientId ->
                    navController.navigate(AppRoutes.visitCreate(savedClientId))
                },
            )
        }
        composable(
            route = AppRoutes.CLIENT_EDIT,
            arguments = AppRoutes.clientIdArguments,
        ) { backStackEntry ->
            ClientFormScreen(
                repository = clientRepository,
                clientId = backStackEntry.arguments?.getString(AppRoutes.CLIENT_ID),
                onBackClick = { navController.navigateUp() },
                onSaved = { navController.navigateUp() },
            )
        }
        composable(
            route = AppRoutes.CLIENT_DETAIL,
            arguments = AppRoutes.clientIdArguments,
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString(AppRoutes.CLIENT_ID).orEmpty()
            ClientDetailScreen(
                clientRepository = clientRepository,
                visitRepository = visitRepository,
                financeRepository = financeRepository,
                clientId = clientId,
                onBackClick = { navController.navigateUp() },
                onEditClick = { navController.navigate(AppRoutes.clientEdit(it)) },
                onScheduleVisitClick = { navController.navigate(AppRoutes.visitCreate(it)) },
                onViewVisitsClick = { navController.navigate(AppRoutes.clientVisits(it)) },
                onViewReceiptsClick = { navController.navigate(AppRoutes.serviceReceipts(it)) },
                onRegisterPaymentClick = { navController.navigate(AppRoutes.registerPayment(clientId = it)) },
                onCreateQuoteClick = { navController.navigate(AppRoutes.quoteCreate(clientId = it)) },
                onCreateMaterialClick = { navController.navigate(AppRoutes.materialCreate(clientId = it)) },
                onVisitClick = { navController.navigateSingleTop(AppRoutes.visitDetail(it)) },
            )
        }
        composable(
            route = AppRoutes.CLIENT_VISITS,
            arguments = AppRoutes.clientIdArguments,
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString(AppRoutes.CLIENT_ID).orEmpty()
            ClientVisitsScreen(
                clientRepository = clientRepository,
                visitRepository = visitRepository,
                clientId = clientId,
                onBackClick = { navController.navigateUp() },
                onAddClick = { navController.navigate(AppRoutes.visitCreate(it)) },
                onVisitClick = { navController.navigate(AppRoutes.visitDetail(it)) },
            )
        }
        composable(
            route = AppRoutes.VISIT_CREATE,
            arguments = AppRoutes.visitCreateArguments,
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString(AppRoutes.CLIENT_ID)?.takeIf { it.isNotBlank() }
            val returnedClientId by backStackEntry.savedStateHandle
                .getStateFlow<String?>(AppRoutes.CREATED_CLIENT_ID, null)
                .collectAsStateWithLifecycle()
            LaunchedEffect(returnedClientId) {
                if (returnedClientId != null) {
                    backStackEntry.savedStateHandle.remove<String>(AppRoutes.CREATED_CLIENT_ID)
                }
            }
            VisitFormScreen(
                clientRepository = clientRepository,
                visitRepository = visitRepository,
                reminderRepository = reminderRepository,
                settingsRepository = settingsRepository,
                reminderCoordinator = reminderCoordinator,
                clientId = clientId,
                returnedClientId = returnedClientId,
                onBackClick = { navController.navigateUp() },
                onCreateClientClick = {
                    navController.navigate(AppRoutes.clientCreate(source = AppRoutes.SOURCE_VISIT))
                },
                onSaved = { navController.navigateUp() },
            )
        }
        composable(
            route = AppRoutes.VISIT_DETAIL,
            arguments = AppRoutes.visitIdArguments,
        ) { backStackEntry ->
            VisitDetailScreen(
                clientRepository = clientRepository,
                visitRepository = visitRepository,
                workSessionRepository = workSessionRepository,
                financeRepository = financeRepository,
                inspectionRepository = inspectionRepository,
                quoteRepository = quoteRepository,
                materialRepository = materialRepository,
                reminderCoordinator = reminderCoordinator,
                visitId = backStackEntry.arguments?.getString(AppRoutes.VISIT_ID).orEmpty(),
                onBackClick = { navController.navigateUp() },
                onHomeClick = { navController.navigateHomeClearingStack() },
                onEditClick = { navController.navigate(AppRoutes.visitEdit(it)) },
                onInspectionClick = { navController.navigateSingleTop(AppRoutes.inspectionOverview(it)) },
                onCreateInspectionClick = { navController.navigateSingleTop(AppRoutes.visitInspectionScope(it)) },
                onCreateQuoteClick = { visitId, clientId ->
                    navController.navigate(AppRoutes.quoteCreate(clientId = clientId, visitId = visitId))
                },
                onQuoteClick = { navController.navigateSingleTop(AppRoutes.quoteDetail(it)) },
                onCreateMaterialClick = { visitId, clientId ->
                    navController.navigate(AppRoutes.materialCreate(clientId = clientId, visitId = visitId))
                },
                onMaterialClick = { navController.navigateSingleTop(AppRoutes.materialDetail(it)) },
                onElectricalToolsClick = { visitId, clientId ->
                    navController.navigateSingleTop(AppRoutes.electricalToolsVoltageDrop(clientId = clientId, visitId = visitId))
                },
                onCloseVisitClick = { navController.navigate(AppRoutes.visitClose(it)) },
                onReceiptClick = { navController.navigateSingleTop(AppRoutes.serviceReceiptDetail(it)) },
                onRegisterPaymentClick = { receiptId, clientId, linkedVisitId ->
                    navController.navigate(AppRoutes.registerPayment(receiptId, clientId, linkedVisitId))
                },
            )
        }
        composable(
            route = AppRoutes.VISIT_EDIT,
            arguments = AppRoutes.visitIdArguments,
        ) { backStackEntry ->
            VisitFormScreen(
                clientRepository = clientRepository,
                visitRepository = visitRepository,
                reminderRepository = reminderRepository,
                settingsRepository = settingsRepository,
                reminderCoordinator = reminderCoordinator,
                clientId = null,
                visitId = backStackEntry.arguments?.getString(AppRoutes.VISIT_ID).orEmpty(),
                onBackClick = { navController.navigateUp() },
                onCreateClientClick = {
                    navController.navigate(AppRoutes.clientCreate(source = AppRoutes.SOURCE_VISIT))
                },
                onSaved = { navController.navigateUp() },
            )
        }
        composable(
            route = AppRoutes.VISIT_INSPECTION_SCOPE,
            arguments = AppRoutes.visitIdArguments,
        ) { backStackEntry ->
            val visitId = backStackEntry.arguments?.getString(AppRoutes.VISIT_ID).orEmpty()
            InspectionScopeSelectionScreen(
                clientRepository = clientRepository,
                visitRepository = visitRepository,
                inspectionRepository = inspectionRepository,
                visitId = visitId,
                onBackClick = { navController.navigateUp() },
                onInspectionReady = { inspectionId ->
                    navController.navigate(AppRoutes.inspectionOverview(inspectionId)) {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(
            route = AppRoutes.INSPECTION_OVERVIEW,
            arguments = AppRoutes.inspectionIdArguments,
        ) { backStackEntry ->
            val inspectionId = backStackEntry.arguments?.getString(AppRoutes.INSPECTION_ID).orEmpty()
            InspectionOverviewScreen(
                inspectionRepository = inspectionRepository,
                visitRepository = visitRepository,
                technicalCalculationRepository = technicalCalculationRepository,
                electricalRuleConfigRepository = electricalRuleConfigRepository,
                inspectionId = inspectionId,
                onBackClick = { navController.navigateUp() },
                onSectionClick = { section ->
                    navController.navigateSingleTop(inspectionSectionRoute(inspectionId, section))
                },
                onCreateQuoteClick = { clientId, visitId ->
                    navController.navigate(AppRoutes.quoteCreate(clientId = clientId, visitId = visitId, inspectionId = inspectionId))
                },
                onCreateMaterialClick = { clientId, visitId ->
                    navController.navigate(AppRoutes.materialCreate(clientId = clientId, visitId = visitId, inspectionId = inspectionId))
                },
                onAddCalculationClick = { clientId, visitId, currentInspectionId, type ->
                    val route = when (type) {
                        InspectionManualCalculationType.POWER_CURRENT_VOLTAGE -> AppRoutes.electricalToolsPower(clientId = clientId, visitId = visitId, inspectionId = currentInspectionId)
                        InspectionManualCalculationType.VOLTAGE_DROP -> AppRoutes.electricalToolsVoltageDrop(clientId = clientId, visitId = visitId, inspectionId = currentInspectionId)
                    }
                    navController.navigate(route)
                },
                onCalculationClick = { navController.navigateSingleTop(AppRoutes.technicalCalculationDetail(it)) },
            )
        }
        composable(AppRoutes.INSPECTION_GENERAL, AppRoutes.inspectionIdArguments) { backStackEntry ->
            InspectionGeneralScreen(inspectionRepository, backStackEntry.inspectionId(), { navController.navigateUp() })
        }
        composable(AppRoutes.INSPECTION_PILLAR, AppRoutes.inspectionIdArguments) { backStackEntry ->
            val inspectionId = backStackEntry.inspectionId()
            PillarInspectionScreen(
                repository = inspectionRepository,
                inspectionId = inspectionId,
                onBackClick = { navController.navigateUp() },
                onPreviousClick = { navController.navigateSingleTop(AppRoutes.inspectionOverview(inspectionId)) },
                onNextClick = { navController.navigateSingleTop(AppRoutes.inspectionMainPanel(inspectionId)) },
                onHomeClick = { navController.navigateSingleTop(AppRoutes.HOME) },
            )
        }
        composable(AppRoutes.INSPECTION_MAIN_PANEL, AppRoutes.inspectionIdArguments) { backStackEntry ->
            val inspectionId = backStackEntry.inspectionId()
            MainPanelInspectionScreen(
                repository = inspectionRepository,
                inspectionId = inspectionId,
                onBackClick = { navController.navigateUp() },
                onPreviousClick = { navController.navigateSingleTop(AppRoutes.inspectionPillar(inspectionId)) },
                onNextClick = { navController.navigateSingleTop(AppRoutes.inspectionFindings(inspectionId)) },
                onHomeClick = { navController.navigateSingleTop(AppRoutes.HOME) },
                onCalculateVoltageDropClick = { navController.navigate(AppRoutes.electricalToolsVoltageDrop(inspectionId = inspectionId)) },
            )
        }
        composable(AppRoutes.INSPECTION_FINDINGS, AppRoutes.inspectionIdArguments) { backStackEntry ->
            val inspectionId = backStackEntry.inspectionId()
            FindingsScreen(
                repository = inspectionRepository,
                inspectionId = inspectionId,
                onBackClick = { navController.navigateUp() },
                onPreviousClick = { navController.navigateSingleTop(AppRoutes.inspectionMainPanel(inspectionId)) },
                onNextClick = { navController.navigateSingleTop(AppRoutes.inspectionFinalReport(inspectionId)) },
                onHomeClick = { navController.navigateSingleTop(AppRoutes.HOME) },
                onAddToQuoteClick = { navController.navigate(AppRoutes.quoteCreate(inspectionId = inspectionId)) },
            )
        }
        composable(AppRoutes.INSPECTION_UNVERIFIED, AppRoutes.inspectionIdArguments) { backStackEntry ->
            UnverifiedItemsScreen(inspectionRepository, backStackEntry.inspectionId(), { navController.navigateUp() })
        }
        composable(AppRoutes.INSPECTION_VISUAL_COMPLEMENTARY, AppRoutes.inspectionIdArguments) { backStackEntry ->
            VisualInspectionComplementaryScreen(inspectionRepository, backStackEntry.inspectionId(), { navController.navigateUp() })
        }
        composable(AppRoutes.INSPECTION_TECHNICAL_COMMENT, AppRoutes.inspectionIdArguments) { backStackEntry ->
            InspectionTechnicalCommentScreen(inspectionRepository, backStackEntry.inspectionId(), { navController.navigateUp() })
        }
        composable(AppRoutes.INSPECTION_FINAL_REPORT, AppRoutes.inspectionIdArguments) { backStackEntry ->
            InspectionFinalReportScreen(inspectionRepository, technicalCalculationRepository, backStackEntry.inspectionId(), { navController.navigateUp() })
        }
    }
}
}


private fun androidx.navigation.NavBackStackEntry.inspectionId(): String {
    return arguments?.getString(AppRoutes.INSPECTION_ID).orEmpty()
}

private fun inspectionSectionRoute(inspectionId: String, section: InspectionSection): String {
    return when (section) {
        InspectionSection.GENERAL -> AppRoutes.inspectionGeneral(inspectionId)
        InspectionSection.PILLAR -> AppRoutes.inspectionPillar(inspectionId)
        InspectionSection.MAIN_PANEL -> AppRoutes.inspectionMainPanel(inspectionId)
        InspectionSection.GROUNDING_SOON -> AppRoutes.inspectionOverview(inspectionId)
        InspectionSection.FINDINGS -> AppRoutes.inspectionFindings(inspectionId)
        InspectionSection.UNVERIFIED -> AppRoutes.inspectionUnverified(inspectionId)
        InspectionSection.VISUAL_COMPLEMENTARY -> AppRoutes.inspectionVisualComplementary(inspectionId)
        InspectionSection.TECHNICAL_COMMENT -> AppRoutes.inspectionTechnicalComment(inspectionId)
        InspectionSection.FINAL_REPORT -> AppRoutes.inspectionFinalReport(inspectionId)
    }
}

private fun androidx.navigation.NavHostController.navigateSingleTop(route: String) {
    navigate(route) { launchSingleTop = true }
}

private fun androidx.navigation.NavHostController.navigateHomeClearingStack() {
    navigate(AppRoutes.HOME) {
        popUpTo(AppRoutes.HOME) { inclusive = false }
        launchSingleTop = true
    }
}
