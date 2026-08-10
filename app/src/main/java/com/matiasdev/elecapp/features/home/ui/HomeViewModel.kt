package com.matiasdev.elecapp.features.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.agenda.domain.localDayBounds
import com.matiasdev.elecapp.features.agenda.ui.VisitAgendaItem
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.materials.data.MaterialRepository
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionRepository
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionDurations
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val workSessionRepository: VisitWorkSessionRepository,
    private val inspectionRepository: InspectionRepository,
    private val quoteRepository: QuoteRepository,
    private val materialRepository: MaterialRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            val today = LocalDate.now()
            val todayBounds = localDayBounds(today)
            val tomorrowBounds = localDayBounds(today.plusDays(1))
            val visitSnapshot = combine(
                visitRepository.observeCurrentInProgressVisit(),
                visitRepository.observeNextFutureVisit(Instant.now().toEpochMilli()),
                visitRepository.observeActiveVisitsInRange(
                    todayBounds.startInclusive.toEpochMilli(),
                    todayBounds.endExclusive.toEpochMilli(),
                ),
                visitRepository.observeActiveVisitsInRange(
                    tomorrowBounds.startInclusive.toEpochMilli(),
                    tomorrowBounds.endExclusive.toEpochMilli(),
                ),
                inspectionRepository.observeDraftInspectionCount(),
            ) { current, next, todayVisits, tomorrowVisits, draftInspectionCount ->
                HomeVisitSnapshot(current, next, todayVisits.size, tomorrowVisits.size, draftInspectionCount)
            }.distinctUntilChanged()
            // Cliente y estado del relevamiento se resuelven cuando cambian las visitas,
            // no cuando cambian las sesiones de trabajo. Estaban dentro del combine con
            // las sesiones, así que iniciar o pausar una visita disparaba cuatro
            // consultas extra aunque la agenda fuera exactamente la misma.
            val agendaSources = visitSnapshot.map { snapshot ->
                HomeAgendaSources(
                    snapshot = snapshot,
                    currentVisitDetails = snapshot.current?.let { visit ->
                        HomeVisitDetails(
                            client = clientRepository.findById(visit.clientId)?.takeUnless { it.isDeleted },
                            inspectionStatus = inspectionRepository.findActiveInspectionForVisit(visit.id)?.status,
                        )
                    },
                    nextVisitDetails = snapshot.next?.let { visit ->
                        HomeVisitDetails(
                            client = clientRepository.findById(visit.clientId)?.takeUnless { it.isDeleted },
                            inspectionStatus = inspectionRepository.findActiveInspectionForVisit(visit.id)?.status,
                        )
                    },
                )
            }
            val agendaState = combine(agendaSources, workSessionRepository.observeAllActive()) { sources, sessions ->
                val now = Instant.now()
                val sessionsByVisitId = sessions.groupBy { it.visitId }
                val snapshot = sources.snapshot
                HomeUiState(
                    isLoading = false,
                    currentVisit = snapshot.current?.let {
                        VisitAgendaItem(
                            visit = it,
                            client = sources.currentVisitDetails?.client,
                            inspectionStatus = sources.currentVisitDetails?.inspectionStatus,
                            workedDuration = VisitWorkSessionDurations.summarize(it, sessionsByVisitId[it.id].orEmpty(), now).totalWorkedDuration,
                        )
                    },
                    nextVisit = snapshot.next?.let {
                        VisitAgendaItem(
                            visit = it,
                            client = sources.nextVisitDetails?.client,
                            inspectionStatus = sources.nextVisitDetails?.inspectionStatus,
                        )
                    },
                    todayCount = snapshot.todayCount,
                    tomorrowCount = snapshot.tomorrowCount,
                    draftInspectionCount = snapshot.draftInspectionCount,
                )
            }
            combine(
                agendaState,
                quoteRepository.observeDraftCount(),
                materialRepository.observeDraftCount(),
            ) { state, draftQuoteCount, draftMaterialListCount ->
                state.copy(
                    draftQuoteCount = draftQuoteCount,
                    draftMaterialListCount = draftMaterialListCount,
                )
            }.catch { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "No se pudo cargar el inicio")
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

private data class HomeVisitDetails(
    val client: com.matiasdev.elecapp.features.clients.domain.Client?,
    val inspectionStatus: com.matiasdev.elecapp.features.inspections.domain.InspectionStatus?,
)

private data class HomeAgendaSources(
    val snapshot: HomeVisitSnapshot,
    val currentVisitDetails: HomeVisitDetails?,
    val nextVisitDetails: HomeVisitDetails?,
)

private data class HomeVisitSnapshot(
    val current: com.matiasdev.elecapp.features.visits.domain.Visit?,
    val next: com.matiasdev.elecapp.features.visits.domain.Visit?,
    val todayCount: Int,
    val tomorrowCount: Int,
    val draftInspectionCount: Int,
)

class HomeViewModelFactory(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val workSessionRepository: VisitWorkSessionRepository,
    private val inspectionRepository: InspectionRepository,
    private val quoteRepository: QuoteRepository,
    private val materialRepository: MaterialRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(
            clientRepository,
            visitRepository,
            workSessionRepository,
            inspectionRepository,
            quoteRepository,
            materialRepository,
        ) as T
    }
}
