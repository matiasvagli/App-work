package com.matiasdev.elecapp.features.inspections.summary

import com.matiasdev.elecapp.features.inspections.domain.ElectricalInspection
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionAggregate
import com.matiasdev.elecapp.features.inspections.domain.MainPanelInspection
import com.matiasdev.elecapp.features.inspections.domain.PillarInspection
import com.matiasdev.elecapp.features.visits.domain.Visit
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object InspectionSummaryGenerator {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun generate(aggregate: InspectionAggregate, visit: Visit?, zoneId: ZoneId = ZoneId.systemDefault()): String {
        val inspection = aggregate.inspection
        return buildString {
            appendHeader(inspection, visit, zoneId)
            appendLine()
            appendLine("TIPO DE RELEVAMIENTO")
            appendLine(inspection.inspectionType.label())
            appendGeneralData(inspection)
            appendPillar(aggregate.pillar)
            appendMainPanel(aggregate.mainPanel)
            appendFindings(aggregate)
            appendUnverified(aggregate)
            appendTechnicalComment(inspection)
            appendLine()
            appendLine("ACLARACIÓN")
            appendLine(
                "Este contenido corresponde a un relevamiento visual y a las verificaciones expresamente registradas. " +
                    "No constituye una certificación integral ni comprende sectores ocultos o inaccesibles.",
            )
        }.trimEnd()
    }

    private fun StringBuilder.appendHeader(
        inspection: ElectricalInspection,
        visit: Visit?,
        zoneId: ZoneId,
    ) {
        appendLine("VISITA TÉCNICA")
        appendLine()
        appendLine("Cliente: ${inspection.clientNameSnapshot}")
        val address = listOf(inspection.addressSnapshot, inspection.localitySnapshot)
            .filter(String::isNotBlank)
            .joinToString(", ")
        appendLineIfNotBlank("Domicilio", address)
        val date = (visit?.scheduledAt ?: inspection.startedAt).atZone(zoneId).format(dateFormatter)
        appendLine("Fecha: $date")
        appendLineIfNotBlank("Motivo", inspection.visitReasonSnapshot)
    }

    private fun StringBuilder.appendGeneralData(inspection: ElectricalInspection) {
        appendLine()
        appendLine("DATOS GENERALES")
        appendLine("- Suministro: ${inspection.supplyType.label()}")
        appendLine("- Tipo de propiedad: ${inspection.propertyType.label()}")
        appendLine("- Estado general: ${inspection.generalCondition.label()}")
        appendLineIfNotBlank("- Técnico", inspection.technicianName)
        appendLineIfNotBlank("- Limitaciones de acceso", inspection.accessLimitations)
    }

    private fun StringBuilder.appendPillar(pillar: PillarInspection?) {
        appendLine()
        appendLine("PILAR Y ACOMETIDA")
        if (pillar == null) {
            appendLine("- No evaluado")
            return
        }
        appendLine("- Existencia: ${pillar.exists?.let { if (it) "sí" else "no" } ?: "no verificada"}")
        appendLine("- Accesibilidad: ${pillar.accessible.label().lowercase()}")
        appendCondition("- Estado general", pillar.generalCondition)
        appendLine("- Térmica principal: ${pillar.mainBreakerPresent.label().lowercase()}")
        pillar.mainBreakerAmps?.let { appendLine("- Térmica principal: $it A") }
        pillar.conductorSectionMm2?.let { appendLine("- Conductores observados: $it mm², ${pillar.conductorMaterial.label().lowercase()}") }
        appendLine("- Estado de conductores: ${pillar.conductorCondition.label().lowercase()}")
        appendLine("- Neutro identificado: ${pillar.neutralIdentified.label().lowercase()}")
        appendLine("- Puesta a tierra visible: ${pillar.groundingVisible.label().lowercase()}")
        appendLine("- Compatibilidad protección/conductor: ${pillar.protectionCompatibility.label().lowercase()}")
        appendLineIfNotBlank("- Observación", pillar.notes)
    }

    private fun StringBuilder.appendMainPanel(panel: MainPanelInspection?) {
        appendLine()
        appendLine("TABLERO PRINCIPAL")
        if (panel == null) {
            appendLine("- No evaluado")
            return
        }
        appendLine("- Accesibilidad: ${panel.accessible.label().lowercase()}")
        appendCondition("- Estado general", panel.generalCondition)
        appendLine("- Interruptor diferencial: ${panel.differentialPresent.label().lowercase()}")
        panel.differentialRatedAmps?.let { appendLine("- Corriente diferencial nominal: $it A") }
        panel.differentialSensitivityMa?.let { appendLine("- Sensibilidad: $it mA") }
        appendLine("- Prueba manual: ${panel.differentialTestResult.label().lowercase()}")
        panel.circuitCount?.let { appendLine("- Cantidad de circuitos: $it") }
        appendLine("- Circuitos identificados: ${panel.circuitsIdentified.label().lowercase()}")
        appendLine("- Barra de neutro: ${panel.neutralBarPresent.label().lowercase()}")
        appendLine("- Barra de tierra: ${panel.groundBarPresent.label().lowercase()}")
        appendLine("- Neutro y tierra separados: ${panel.neutralAndGroundSeparated.label().lowercase()}")
        appendLine("- Empalmes improvisados: ${panel.improvisedConnections.label().lowercase()}")
        appendLine("- Colores incorrectos o mezclados: ${panel.mixedOrIncorrectColors.label().lowercase()}")
        appendLine("- Signos de recalentamiento: ${panel.overheatingSigns.label().lowercase()}")
        appendLine("- Compatibilidad protección/conductor: ${panel.protectionCompatibility.label().lowercase()}")
        appendLineIfNotBlank("- Observación", panel.notes)
    }

    private fun StringBuilder.appendFindings(aggregate: InspectionAggregate) {
        appendLine()
        appendLine("HALLAZGOS")
        if (aggregate.findings.isEmpty()) {
            appendLine("- Sin hallazgos registrados")
            return
        }
        aggregate.findings.forEach { finding ->
            appendLine("[${finding.severity.label().uppercase()}] ${finding.title}")
            appendLine("Categoría: ${finding.category.label()}")
            appendLine("Descripción: ${finding.description}")
            appendLineIfNotBlank("Recomendación", finding.recommendation)
            appendLine()
        }
    }

    private fun StringBuilder.appendUnverified(aggregate: InspectionAggregate) {
        appendLine("NO VERIFICADO")
        appendLine("Estos elementos no fueron verificados durante la visita.")
        if (aggregate.unverifiedItems.isEmpty()) {
            appendLine("- Sin elementos registrados")
            return
        }
        aggregate.unverifiedItems.forEach { item ->
            val suffix = item.description?.takeIf(String::isNotBlank)?.let { ": $it" }.orEmpty()
            appendLine("- ${item.type.label()}$suffix")
        }
    }

    private fun StringBuilder.appendTechnicalComment(inspection: ElectricalInspection) {
        if (inspection.originalTechnicalComment.isNullOrBlank()) return
        appendLine()
        appendLine("COMENTARIO ORIGINAL DEL ELECTRICISTA")
        appendLine(inspection.originalTechnicalComment)
    }

    private fun StringBuilder.appendCondition(label: String, condition: GeneralCondition) {
        appendLine("$label: ${condition.label().lowercase()}")
    }

    private fun StringBuilder.appendLineIfNotBlank(label: String, value: String?) {
        if (!value.isNullOrBlank()) appendLine("$label: $value")
    }
}
