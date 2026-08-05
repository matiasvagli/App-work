package com.matiasdev.elecapp.features.agenda.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.agenda.domain.groupUpcomingVisits
import com.matiasdev.elecapp.features.agenda.domain.localDayBounds
import com.matiasdev.elecapp.features.agenda.domain.todaySections
import com.matiasdev.elecapp.features.agenda.domain.upcomingVisits
import com.matiasdev.elecapp.features.agenda.domain.visitsForDate
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionRepository
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSession
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionDurations
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModel(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val workSessionRepository: VisitWorkSessionRepository,
    private val inspectionRepository: InspectionRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgendaUiState())
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()
    private val month = MutableStateFlow(YearMonth.now())

    init {
        viewModelScope.launch(ioDispatcher) {
            val todayBounds = localDayBounds(LocalDate.now())
            val todayFlow = visitRepository.observeActiveVisitsInRange(
                todayBounds.startInclusive.toEpochMilli(),
                todayBounds.endExclusive.toEpochMilli(),
            )
            val upcomingFlow = visitRepository.observeActiveVisitsFrom(Instant.now().toEpochMilli())
            val calendarFlow = month.flatMapLatest { yearMonth ->
                val start = yearMonth.atDay(1)
                val bounds = localDayBounds(start)
                val endBounds = localDayBounds(start.plusMonths(1))
                visitRepository.observeActiveVisitsInRange(
                    bounds.startInclusive.toEpochMilli(),
                    endBounds.startInclusive.toEpochMilli(),
                )
            }

            combine(todayFlow, upcomingFlow, calendarFlow, month, workSessionRepository.observeAllActive()) { today, upcoming, calendar, visibleMonth, sessions ->
                buildState(today, upcoming, calendar, visibleMonth, sessions)
            }.catch { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "No se pudo cargar la agenda")
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onModeChange(mode: AgendaMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun onSelectedDateChange(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun previousMonth() {
        month.update { it.minusMonths(1) }
    }

    fun nextMonth() {
        month.update { it.plusMonths(1) }
    }

    private suspend fun buildState(
        todayVisits: List<Visit>,
        upcoming: List<Visit>,
        calendar: List<Visit>,
        visibleMonth: YearMonth,
        sessions: List<VisitWorkSession>,
    ): AgendaUiState {
        val sessionsByVisitId = sessions.groupBy { it.visitId }
        val todayItems = todayVisits.toAgendaItems(sessionsByVisitId)
        val sections = todaySections(todayVisits)
        val upcomingFiltered = upcomingVisits(upcoming)
        val upcomingItems = upcomingFiltered.toAgendaItems(sessionsByVisitId)
        return _uiState.value.copy(
            isLoading = false,
            visibleMonth = visibleMonth,
            todayUpcoming = todayItems.filter { item -> item.visit in sections.upcoming },
            todayDoneOrPast = todayItems.filter { item -> item.visit in sections.doneOrPast },
            upcomingGroups = groupUpcomingVisits(upcomingFiltered),
            upcomingItemsByVisitId = upcomingItems.associateBy { it.visit.id },
            calendarItems = calendar.toAgendaItems(sessionsByVisitId),
            errorMessage = null,
        )
    }

    private suspend fun List<Visit>.toAgendaItems(sessionsByVisitId: Map<String, List<VisitWorkSession>>): List<VisitAgendaItem> {
        val now = Instant.now()
        return withContext(ioDispatcher) {
            map { visit ->
                VisitAgendaItem(
                    visit = visit,
                    client = clientRepository.findById(visit.clientId)?.takeUnless { it.isDeleted },
                    inspectionStatus = inspectionRepository.findActiveInspectionForVisit(visit.id)?.status,
                    workedDuration = VisitWorkSessionDurations.summarize(visit, sessionsByVisitId[visit.id].orEmpty(), now).totalWorkedDuration,
                )
            }
        }
    }
}

class AgendaViewModelFactory(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val workSessionRepository: VisitWorkSessionRepository,
    private val inspectionRepository: InspectionRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AgendaViewModel(clientRepository, visitRepository, workSessionRepository, inspectionRepository) as T
    }
}
