package com.matiasdev.elecapp.features.agenda.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.agenda.domain.visitsForDate
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionRepository
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.ui.formatCompactDuration
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    workSessionRepository: VisitWorkSessionRepository,
    inspectionRepository: InspectionRepository,
    onBackClick: () -> Unit,
    onCreateVisitClick: () -> Unit,
    onVisitClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AgendaViewModel = viewModel(
        factory = AgendaViewModelFactory(clientRepository, visitRepository, workSessionRepository, inspectionRepository),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Agenda") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateVisitClick) {
                Icon(Icons.Default.Add, contentDescription = "Nueva visita")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            PrimaryTabRow(selectedTabIndex = uiState.mode.ordinal) {
                AgendaMode.entries.forEach { mode ->
                    Tab(
                        selected = uiState.mode == mode,
                        onClick = { viewModel.onModeChange(mode) },
                        text = { Text(mode.label) },
                    )
                }
            }
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                uiState.errorMessage != null -> Text(uiState.errorMessage.orEmpty(), modifier = Modifier.padding(24.dp))
                uiState.mode == AgendaMode.TODAY -> TodayAgenda(uiState, onVisitClick)
                uiState.mode == AgendaMode.UPCOMING -> UpcomingAgenda(uiState, onVisitClick)
                uiState.mode == AgendaMode.CALENDAR -> CalendarAgenda(
                    uiState = uiState,
                    onPreviousMonth = viewModel::previousMonth,
                    onNextMonth = viewModel::nextMonth,
                    onSelectedDateChange = viewModel::onSelectedDateChange,
                    onVisitClick = onVisitClick,
                )
            }
        }
    }
}

@Composable
private fun TodayAgenda(uiState: AgendaUiState, onVisitClick: (String) -> Unit) {
    if (uiState.todayUpcoming.isEmpty() && uiState.todayDoneOrPast.isEmpty()) {
        EmptyAgenda("No tenés visitas para hoy")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp)) {
        if (uiState.todayUpcoming.isNotEmpty()) {
            item { SectionTitle("Próximas") }
            items(uiState.todayUpcoming, key = { it.visit.id }) { item -> VisitRow(item, onVisitClick) }
        }
        if (uiState.todayDoneOrPast.isNotEmpty()) {
            item { SectionTitle("Ya realizadas o vencidas") }
            items(uiState.todayDoneOrPast, key = { it.visit.id }) { item -> VisitRow(item, onVisitClick) }
        }
    }
}

@Composable
private fun UpcomingAgenda(uiState: AgendaUiState, onVisitClick: (String) -> Unit) {
    if (uiState.upcomingGroups.isEmpty()) {
        EmptyAgenda("No hay visitas próximas")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp)) {
        uiState.upcomingGroups.forEach { group ->
            item { SectionTitle(group.title) }
            items(group.visits, key = { it.id }) { visit ->
                uiState.upcomingItemsByVisitId[visit.id]?.let { VisitRow(it, onVisitClick) }
            }
        }
    }
}

@Composable
private fun CalendarAgenda(
    uiState: AgendaUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectedDateChange: (LocalDate) -> Unit,
    onVisitClick: (String) -> Unit,
) {
    val selectedVisits = visitsForDate(uiState.calendarItems.map { it.visit }, uiState.selectedDate)
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onPreviousMonth) { Text("Anterior") }
            Text(
                DateTimeFormatter.ofPattern("MMMM yyyy").format(uiState.visibleMonth),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = onNextMonth) { Text("Siguiente") }
        }
        MonthGrid(
            month = uiState.visibleMonth,
            selectedDate = uiState.selectedDate,
            datesWithVisits = uiState.calendarItems.map { it.visit.scheduledAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate() }.toSet(),
            onSelectedDateChange = onSelectedDateChange,
        )
        SectionTitle(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(uiState.selectedDate))
        if (selectedVisits.isEmpty()) {
            Text("No hay visitas para este día", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            selectedVisits.forEach { visit ->
                uiState.calendarItems.firstOrNull { it.visit.id == visit.id }?.let { VisitRow(it, onVisitClick) }
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    datesWithVisits: Set<LocalDate>,
    onSelectedDateChange: (LocalDate) -> Unit,
) {
    val firstDayOffset = (month.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value).floorMod(7)
    val cells = List(firstDayOffset) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { date ->
                    val label = date?.dayOfMonth?.toString().orEmpty()
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = date != null) { date?.let(onSelectedDateChange) },
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(label, fontWeight = if (date == selectedDate) FontWeight.Bold else FontWeight.Normal)
                            if (date in datesWithVisits) Text("Visita", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                repeat(7 - week.size) {
                    Column(modifier = Modifier.weight(1f)) {}
                }
            }
        }
    }
}

@Composable
private fun VisitRow(item: VisitAgendaItem, onVisitClick: (String) -> Unit) {
    ListItem(
        headlineContent = { Text("${item.visit.scheduledAt.formatAgendaTime()} · ${item.clientName}") },
        supportingContent = {
            Text(
                listOf(
                    item.visit.reason,
                    item.location,
                    "Relevamiento: ${item.inspectionStatus.labelForAgenda()}",
                ).filter(String::isNotBlank).joinToString(" · "),
            )
            if (item.visit.status == VisitStatus.IN_PROGRESS) {
                item.workedDuration?.let { Text("En curso · ${it.formatCompactDuration()} trabajados") }
            }
        },
        trailingContent = { VisitStatusChip(item.visit) },
        modifier = Modifier.clickable { onVisitClick(item.visit.id) },
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun EmptyAgenda(text: String) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun Int.floorMod(other: Int): Int = ((this % other) + other) % other

private fun java.time.Instant.formatAgendaTime(): String {
    return DateTimeFormatter.ofPattern("HH:mm").format(atZone(java.time.ZoneId.systemDefault()))
}

private fun InspectionStatus?.labelForAgenda(): String = when (this) {
    null -> "Sin relevamiento"
    InspectionStatus.DRAFT -> "Borrador"
    InspectionStatus.COMPLETED -> "Finalizado"
}
