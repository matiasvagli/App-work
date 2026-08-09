package com.matiasdev.elecapp.features.finance.domain

import com.matiasdev.elecapp.core.time.SystemTimeProvider
import com.matiasdev.elecapp.core.time.TimeProvider
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfigRepository
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.AutoInspectionCalculationBuilder
import com.matiasdev.elecapp.features.inspections.summary.InspectionSummaryGenerator
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import kotlinx.coroutines.flow.first

/**
 * Congela el informe técnico de una atención.
 *
 * Cruza relevamientos, reglas, visitas y finanzas sin acoplar esos repositorios entre
 * sí: depende solo de las interfaces y se arma en `AppContainer`, igual que
 * [com.matiasdev.elecapp.features.reminders.scheduling.ReminderCoordinator].
 *
 * El informe es un derivado, no una fuente de verdad. Por eso [generateForClosedVisit]
 * nunca propaga errores: si falla, la atención igual quedó cerrada y el informe se
 * puede regenerar después. Fallar el cierre por un texto que se puede rehacer sería
 * el peor intercambio posible.
 */
class AttentionReportCoordinator(
    private val inspectionRepository: InspectionRepository,
    private val visitRepository: VisitRepository,
    private val financeRepository: FinanceRepository,
    private val ruleConfigRepository: ElectricalRuleConfigRepository,
    private val timeProvider: TimeProvider = SystemTimeProvider,
) {

    /**
     * Genera y persiste el snapshot de una visita ya cerrada.
     *
     * @return true si quedó guardado. false si no había nada que congelar (una atención
     *   sin relevamiento no tiene informe técnico: su registro en la historia clínica
     *   son los campos de trabajo del cierre) o si algo falló.
     */
    suspend fun generateForClosedVisit(visitId: String): Boolean =
        runCatching { generate(visitId) }.getOrDefault(false)

    private suspend fun generate(visitId: String): Boolean {
        val inspection = inspectionRepository.findActiveInspectionForVisit(visitId) ?: return false
        val aggregate = inspectionRepository.findAggregate(inspection.id) ?: return false
        val visit = visitRepository.findActiveById(visitId)
        val completion = financeRepository.observeVisitCompletion(visitId).first() ?: return false
        val rules = ruleConfigRepository.observeAll().first()

        val snapshot = InspectionSummaryGenerator.generate(
            aggregate = aggregate,
            visit = visit,
            visitCompletion = completion,
            autoCalculations = AutoInspectionCalculationBuilder.build(aggregate, rules),
        )
        if (snapshot.isBlank()) return false

        financeRepository.saveTechnicalReportSnapshot(
            visitId = visitId,
            snapshot = snapshot,
            generatedAt = timeProvider.now(),
        )
        return true
    }
}
