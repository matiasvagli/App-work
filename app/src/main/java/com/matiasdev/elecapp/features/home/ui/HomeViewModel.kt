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
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
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
            val agendaState = combine(
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
                HomeUiState(
                    isLoading = false,
                    currentVisit = current?.let {
                        VisitAgendaItem(
                            visit = it,
                            client = clientRepository.findById(it.clientId)?.takeUnless { client -> client.isDeleted },
                            inspectionStatus = inspectionRepository.findActiveInspectionForVisit(it.id)?.status,
                        )
                    },
                    nextVisit = next?.let {
                        VisitAgendaItem(
                            visit = it,
                            client = clientRepository.findById(it.clientId)?.takeUnless { client -> client.isDeleted },
                            inspectionStatus = inspectionRepository.findActiveInspectionForVisit(it.id)?.status,
                        )
                    },
                    todayCount = todayVisits.size,
                    tomorrowCount = tomorrowVisits.size,
                    draftInspectionCount = draftInspectionCount,
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

class HomeViewModelFactory(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val inspectionRepository: InspectionRepository,
    private val quoteRepository: QuoteRepository,
    private val materialRepository: MaterialRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(
            clientRepository,
            visitRepository,
            inspectionRepository,
            quoteRepository,
            materialRepository,
        ) as T
    }
}
