package com.matiasdev.elecapp.features.agenda.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.ui.components.ElecBadge
import com.matiasdev.elecapp.core.ui.components.ElecLoadingState
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                title = { Text("Agenda de visitas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateVisitClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva visita")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            PrimaryTabRow(
                selectedTabIndex = uiState.mode.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                AgendaMode.entries.forEach { mode ->
                    Tab(
                        selected = uiState.mode == mode,
                        onClick = { viewModel.onModeChange(mode) },
                        text = { Text(mode.label, fontWeight = if (uiState.mode == mode) FontWeight.Bold else FontWeight.Normal) },
                    )
                }
            }
            when {
                uiState.isLoading -> ElecLoadingState("Cargando visitas...")
                uiState.errorMessage != null -> Text(uiState.errorMessage.orEmpty(), modifier = Modifier.padding(24.dp), color = MaterialTheme.colorScheme.error)
                uiState.mode == AgendaMode.CALENDAR -> CalendarAgenda(
                    uiState = uiState,
                    onPreviousMonth = viewModel::previousMonth,
                    onNextMonth = viewModel::nextMonth,
                    onSelectedDateChange = viewModel::onSelectedDateChange,
                    onVisitClick = onVisitClick,
                    onCreateVisitClick = onCreateVisitClick,
                )
                uiState.mode == AgendaMode.TODAY -> TodayAgenda(uiState, onVisitClick, onCreateVisitClick)
                uiState.mode == AgendaMode.UPCOMING -> UpcomingAgenda(uiState, onVisitClick)
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
    onCreateVisitClick: () -> Unit,
) {
    val selectedVisits = visitsForDate(uiState.calendarItems.map { it.visit }, uiState.selectedDate)
    val visitsByDate = uiState.calendarItems
        .groupBy { it.visit.scheduledAt.atZone(ZoneId.systemDefault()).toLocalDate() }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onPreviousMonth) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Mes anterior")
                            }
                            Text(
                                text = uiState.visibleMonth.formatMonthYear(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            IconButton(onClick = onNextMonth) {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Mes siguiente")
                            }
                        }
                        if (uiState.selectedDate != LocalDate.now() || uiState.visibleMonth != YearMonth.now()) {
                            FilledTonalButton(
                                onClick = {
                                    onSelectedDateChange(LocalDate.now())
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text("Hoy", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    MonthGrid(
                        month = uiState.visibleMonth,
                        selectedDate = uiState.selectedDate,
                        visitsByDate = visitsByDate,
                        onSelectedDateChange = onSelectedDateChange,
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = uiState.selectedDate.formatSpanishDate(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                val badgeText = "${selectedVisits.size} ${if (selectedVisits.size == 1) "visita" else "visitas"}"
                if (selectedVisits.isNotEmpty()) {
                    ElecBadge(
                        text = badgeText,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                } else {
                    ElecBadge(
                        text = badgeText,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (selectedVisits.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp),
                        )
                        Text(
                            text = "No tenés trabajos agendados para este día",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        OutlinedButton(
                            onClick = onCreateVisitClick,
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Agendar visita")
                        }
                    }
                }
            }
        } else {
            items(selectedVisits, key = { it.id }) { visit ->
                uiState.calendarItems.firstOrNull { it.visit.id == visit.id }?.let { item ->
                    VisitAgendaCard(
                        item = item,
                        onVisitClick = onVisitClick,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    visitsByDate: Map<LocalDate, List<VisitAgendaItem>>,
    onSelectedDateChange: (LocalDate) -> Unit,
) {
    val firstDayOffset = (month.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value).floorMod(7)
    val cells = List(firstDayOffset) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { date ->
                    if (date != null) {
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()
                        val visitsForDayCount = visitsByDate[date]?.size ?: 0

                        val containerColor = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isToday -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val textColor = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        val border = if (isToday && !isSelected) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        } else null

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectedDateChange(date) },
                            shape = RoundedCornerShape(12.dp),
                            color = containerColor,
                            border = border,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(2.dp),
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor,
                                )
                                if (visitsForDayCount > 0) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                shape = CircleShape,
                                            ),
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TodayAgenda(
    uiState: AgendaUiState,
    onVisitClick: (String) -> Unit,
    onCreateVisitClick: () -> Unit,
) {
    if (uiState.todayUpcoming.isEmpty() && uiState.todayDoneOrPast.isEmpty()) {
        EmptyAgenda("No tenés visitas programadas para hoy", onCreateVisitClick)
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (uiState.todayUpcoming.isNotEmpty()) {
            item { SectionTitle("Pendientes de hoy") }
            items(uiState.todayUpcoming, key = { it.visit.id }) { item ->
                VisitAgendaCard(item = item, onVisitClick = onVisitClick)
            }
        }
        if (uiState.todayDoneOrPast.isNotEmpty()) {
            item { SectionTitle("Realizadas o pasadas de hoy") }
            items(uiState.todayDoneOrPast, key = { it.visit.id }) { item ->
                VisitAgendaCard(item = item, onVisitClick = onVisitClick)
            }
        }
    }
}

@Composable
private fun UpcomingAgenda(uiState: AgendaUiState, onVisitClick: (String) -> Unit) {
    if (uiState.upcomingGroups.isEmpty()) {
        EmptyAgenda("No hay visitas próximas agendadas")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        uiState.upcomingGroups.forEach { group ->
            item { SectionTitle(group.title) }
            items(group.visits, key = { it.id }) { visit ->
                uiState.upcomingItemsByVisitId[visit.id]?.let { item ->
                    VisitAgendaCard(item = item, onVisitClick = onVisitClick)
                }
            }
        }
    }
}

@Composable
private fun VisitAgendaCard(
    item: VisitAgendaItem,
    onVisitClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick = { onVisitClick(item.visit.id) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.visit.scheduledAt.formatAgendaTime()} hs",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                VisitStatusChip(visit = item.visit)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.clientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (item.visit.reason.isNotBlank()) {
                Text(
                    text = item.visit.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (item.location.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                item.inspectionStatus?.let { status ->
                    val (bgColor, textColor) = when (status) {
                        InspectionStatus.COMPLETED -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
                        InspectionStatus.DRAFT -> Color(0xFFFFF3E0) to Color(0xFFE65100)
                    }
                    ElecBadge(
                        text = "Relevamiento: ${status.labelForAgenda()}",
                        containerColor = bgColor,
                        contentColor = textColor,
                    )
                }
                if (item.visit.status == VisitStatus.IN_PROGRESS && item.workedDuration != null) {
                    Text(
                        text = "En curso (${item.workedDuration.formatCompactDuration()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun EmptyAgenda(text: String, onCreateVisitClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (onCreateVisitClick != null) {
                OutlinedButton(
                    onClick = onCreateVisitClick,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Agendar visita")
                }
            }
        }
    }
}

private fun Int.floorMod(other: Int): Int = ((this % other) + other) % other

private fun java.time.Instant.formatAgendaTime(): String {
    return DateTimeFormatter.ofPattern("HH:mm").format(atZone(ZoneId.systemDefault()))
}

private fun YearMonth.formatMonthYear(): String {
    val spanishLocale = Locale("es", "ES")
    val monthName = DateTimeFormatter.ofPattern("MMMM", spanishLocale).format(this)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(spanishLocale) else it.toString() }
    return "$monthName $year"
}

private fun LocalDate.formatSpanishDate(): String {
    val spanishLocale = Locale("es", "ES")
    val dayName = DateTimeFormatter.ofPattern("EEEE", spanishLocale).format(this)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(spanishLocale) else it.toString() }
    val dayNum = dayOfMonth
    val monthName = DateTimeFormatter.ofPattern("MMMM", spanishLocale).format(this)
    return "$dayName $dayNum de $monthName"
}

private fun InspectionStatus.labelForAgenda(): String = when (this) {
    InspectionStatus.DRAFT -> "Borrador"
    InspectionStatus.COMPLETED -> "Finalizado"
}


