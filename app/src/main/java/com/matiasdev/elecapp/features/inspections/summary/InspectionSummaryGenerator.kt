package com.matiasdev.elecapp.features.inspections.summary

import com.matiasdev.elecapp.features.inspections.domain.ElectricalInspection
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.summary.TechnicalCalculationTextGenerator
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionAggregate
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.MainPanelInspection
import com.matiasdev.elecapp.features.inspections.domain.PillarInspection
import com.matiasdev.elecapp.features.visits.domain.Visit
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object InspectionSummaryGenerator {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun generate(
        aggregate: InspectionAggregate,
        visit: Visit?,
        zoneId: ZoneId = ZoneId.systemDefault(),
        calculations: List<TechnicalCalculation> = aggregate.calculations,
    ): String {
        val inspection = aggregate.inspection
        if (inspection.scope == InspectionScope.VISUAL_INSPECTION) {
            return generateVisualInspection(aggregate, visit, zoneId, calculations)
        }
        return buildString {
            appendHeader(inspection, visit, zoneId)
            appendLine()
            appendLine("TIPO DE RELEVAMIENTO")
            appendLine(inspection.inspectionType.label())
            appendGeneralData(inspection)
            appendPillar(aggregate.pillar)
            appendMainPanel(aggregate.mainPanel)
            appendCalculations(calculations)
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

    private fun generateVisualInspection(
        aggregate: InspectionAggregate,
        visit: Visit?,
        zoneId: ZoneId,
        calculations: List<TechnicalCalculation>,
    ): String {
        val inspection = aggregate.inspection
        return buildString {
            appendLine("INSPECCIÓN VISUAL")
            appendLine()
            appendLine("Cliente: ${inspection.clientNameSnapshot}")
            val date = (visit?.scheduledAt ?: inspection.startedAt).atZone(zoneId).format(dateFormatter)
            appendLine("Fecha: $date")
            appendLineIfNotBlank("Motivo", inspection.reviewReason ?: inspection.visitReasonSnapshot)
            appendLineIfNotBlank("Sector o elemento revisado", inspection.reviewedElement)
            appendLineIfNotBlank("Descripción", inspection.taskDescription)
            appendVisualGeneralData(inspection)
            appendVisualPillar(aggregate.pillar)
            appendVisualMainPanel(aggregate.mainPanel)
            appendVisualCalculations(calculations)
            appendVisualFindings(aggregate)
            appendVisualUnverified(aggregate)
            appendVisualObservation(inspection)
            appendLine()
            appendLine("ACLARACIÓN")
            appendLine(
                "La revisión se limitó al sector, a los componentes visibles y a las verificaciones expresamente registradas. " +
                    "No se realizó una evaluación integral de la instalación ni se verificaron sectores ocultos, inaccesibles o no incluidos en la visita.",
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

    private fun StringBuilder.appendVisualGeneralData(inspection: ElectricalInspection) {
        val rows = buildList {
            if (inspection.supplyType != com.matiasdev.elecapp.features.inspections.domain.SupplyType.UNKNOWN) {
                add("- Suministro: ${inspection.supplyType.label()}")
            }
            if (inspection.propertyType != com.matiasdev.elecapp.features.inspections.domain.PropertyType.UNKNOWN) {
                add("- Tipo de propiedad: ${inspection.propertyType.label()}")
            }
            if (inspection.generalCondition != GeneralCondition.NOT_ASSESSED) {
                add("- Estado general observado: ${inspection.generalCondition.label()}")
            }
            inspection.technicianName?.takeIf(String::isNotBlank)?.let { add("- Técnico: $it") }
        }
        if (rows.isEmpty()) return
        appendLine()
        appendLine("DATOS BÁSICOS")
        rows.forEach(::appendLine)
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

    private fun StringBuilder.appendVisualPillar(pillar: PillarInspection?) {
        if (pillar == null || !pillar.hasVisualContent()) return
        appendLine()
        appendLine("PILAR Y ACOMETIDA")
        if (pillar.reviewStatus != InspectionSectionReviewStatus.REVIEWED) {
            appendLine("- Estado de revisión: ${pillar.reviewStatus.label()}")
            return
        }
        pillar.exists?.let { appendLine("- Existencia: ${if (it) "Sí" else "No"}") }
        appendVisualAccess("- Accesibilidad", pillar.accessible)
        appendVisualCondition("- Estado general", pillar.generalCondition)
        appendVisualYesNo("- Térmica principal visible", pillar.mainBreakerPresent)
        pillar.mainBreakerAmps?.let { appendLine("- Corriente nominal: $it A") }
        pillar.conductorSectionMm2?.let { appendLine("- Sección visible del conductor: ${formatNumber(it)} mm²") }
        appendVisualEnum("- Material del conductor", pillar.conductorMaterial, com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial.UNKNOWN)
        appendVisualEnum("- Estado visible de los conductores", pillar.conductorCondition, com.matiasdev.elecapp.features.inspections.domain.ConductorCondition.NOT_ASSESSED)
        appendVisualYesNo("- Neutro identificado", pillar.neutralIdentified)
        appendVisualYesNo("- Puesta a tierra visible", pillar.groundingVisible)
        appendVisualEnum("- Compatibilidad protección/conductor", pillar.protectionCompatibility, com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility.NOT_ASSESSED)
        appendLineIfNotBlank("- Observaciones", pillar.notes)
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

    private fun StringBuilder.appendVisualMainPanel(panel: MainPanelInspection?) {
        if (panel == null || !panel.hasVisualContent()) return
        appendLine()
        appendLine("TABLERO PRINCIPAL")
        if (panel.reviewStatus != InspectionSectionReviewStatus.REVIEWED) {
            appendLine("- Estado de revisión: ${panel.reviewStatus.label()}")
            return
        }
        appendVisualAccess("- Accesibilidad", panel.accessible)
        appendVisualCondition("- Estado general", panel.generalCondition)
        appendVisualYesNo("- Interruptor diferencial visible", panel.differentialPresent)
        panel.differentialRatedAmps?.let { appendLine("- Corriente nominal: $it A") }
        panel.differentialSensitivityMa?.let { appendLine("- Sensibilidad: $it mA") }
        appendLine("- Prueba manual: ${panel.differentialTestResult.label()}")
        panel.circuitCount?.let { appendLine("- Cantidad visible de circuitos: $it") }
        appendVisualYesNoPartial("- Circuitos identificados", panel.circuitsIdentified)
        appendVisualYesNo("- Barra de neutro visible", panel.neutralBarPresent)
        appendVisualYesNo("- Barra de tierra visible", panel.groundBarPresent)
        appendVisualYesNo("- Neutro y tierra aparentemente separados", panel.neutralAndGroundSeparated)
        appendVisualYesNo("- Empalmes improvisados visibles", panel.improvisedConnections)
        appendVisualYesNo("- Colores incorrectos o mezclados", panel.mixedOrIncorrectColors)
        appendVisualYesNo("- Signos visibles de recalentamiento", panel.overheatingSigns)
        appendVisualEnum("- Compatibilidad protección/conductor", panel.protectionCompatibility, com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility.NOT_ASSESSED)
        appendLineIfNotBlank("- Observaciones", panel.notes)
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

    private fun StringBuilder.appendVisualFindings(aggregate: InspectionAggregate) {
        if (aggregate.findings.isEmpty()) return
        appendLine()
        appendLine("HALLAZGOS")
        aggregate.findings.forEach { finding ->
            appendLine("[${finding.severity.label().uppercase()}] ${finding.title}")
            appendLine("Categoría: ${finding.category.label()}")
            appendLine("Descripción: ${finding.description}")
            appendLineIfNotBlank("Recomendación", finding.recommendation)
            appendLine()
        }
    }

    private fun StringBuilder.appendCalculations(calculations: List<TechnicalCalculation>) {
        appendLine()
        appendLine("MEDICIONES Y CÁLCULOS")
        val activeCalculations = calculations.filterNot { it.isDeleted }.sortedBy { it.createdAt }
        if (activeCalculations.isEmpty()) {
            appendLine("- Sin mediciones ni cálculos asociados")
            return
        }
        activeCalculations.forEach { calculation ->
            appendLine(TechnicalCalculationTextGenerator.generate(calculation).prependIndent(""))
            appendLine()
        }
    }

    private fun StringBuilder.appendVisualCalculations(calculations: List<TechnicalCalculation>) {
        val activeCalculations = calculations.filterNot { it.isDeleted }.sortedBy { it.createdAt }
        if (activeCalculations.isEmpty()) return
        appendLine()
        appendLine("MEDICIONES Y CÁLCULOS")
        activeCalculations.forEach { calculation ->
            appendLine(TechnicalCalculationTextGenerator.generate(calculation).prependIndent(""))
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

    private fun StringBuilder.appendVisualUnverified(aggregate: InspectionAggregate) {
        if (aggregate.unverifiedItems.isEmpty()) return
        appendLine()
        appendLine("NO VERIFICADO")
        aggregate.unverifiedItems.forEach { item ->
            val suffix = item.description?.takeIf(String::isNotBlank)?.let { ": $it" }.orEmpty()
            appendLine("- ${item.type.label()}$suffix")
        }
    }

    private fun StringBuilder.appendVisualObservation(inspection: ElectricalInspection) {
        if (inspection.originalTechnicalComment.isNullOrBlank()) return
        appendLine()
        appendLine("OBSERVACIONES")
        appendLine(inspection.originalTechnicalComment)
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

    private fun StringBuilder.appendVisualAccess(label: String, value: com.matiasdev.elecapp.features.inspections.domain.AccessStatus) {
        if (value != com.matiasdev.elecapp.features.inspections.domain.AccessStatus.UNKNOWN) appendLine("$label: ${value.label()}")
    }

    private fun StringBuilder.appendVisualCondition(label: String, value: GeneralCondition) {
        if (value != GeneralCondition.NOT_ASSESSED) appendLine("$label: ${value.label()}")
    }

    private fun StringBuilder.appendVisualYesNo(label: String, value: com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown) {
        if (value != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN) appendLine("$label: ${value.label()}")
    }

    private fun StringBuilder.appendVisualYesNoPartial(label: String, value: com.matiasdev.elecapp.features.inspections.domain.YesNoPartialUnknown) {
        if (value != com.matiasdev.elecapp.features.inspections.domain.YesNoPartialUnknown.UNKNOWN) appendLine("$label: ${value.label()}")
    }

    private inline fun <reified T : Enum<T>> StringBuilder.appendVisualEnum(label: String, value: T, emptyValue: T) {
        if (value != emptyValue) appendLine("$label: ${value.labelForVisualEnum()}")
    }

    private fun Enum<*>.labelForVisualEnum(): String {
        return when (this) {
            is com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial -> label()
            is com.matiasdev.elecapp.features.inspections.domain.ConductorCondition -> label()
            is com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility -> label()
            else -> name
        }
    }

    private fun PillarInspection.hasVisualContent(): Boolean {
        return reviewStatus != InspectionSectionReviewStatus.REVIEWED ||
            exists != null ||
            accessible != com.matiasdev.elecapp.features.inspections.domain.AccessStatus.UNKNOWN ||
            generalCondition != GeneralCondition.NOT_ASSESSED ||
            mainBreakerPresent != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            mainBreakerAmps != null ||
            conductorSectionMm2 != null ||
            conductorMaterial != com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial.UNKNOWN ||
            conductorCondition != com.matiasdev.elecapp.features.inspections.domain.ConductorCondition.NOT_ASSESSED ||
            neutralIdentified != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            groundingVisible != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            protectionCompatibility != com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility.NOT_ASSESSED ||
            !notes.isNullOrBlank()
    }

    private fun MainPanelInspection.hasVisualContent(): Boolean {
        return reviewStatus != InspectionSectionReviewStatus.REVIEWED ||
            accessible != com.matiasdev.elecapp.features.inspections.domain.AccessStatus.UNKNOWN ||
            generalCondition != GeneralCondition.NOT_ASSESSED ||
            differentialPresent != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            differentialRatedAmps != null ||
            differentialSensitivityMa != null ||
            differentialTestResult != com.matiasdev.elecapp.features.inspections.domain.DifferentialTestResult.NOT_TESTED ||
            circuitCount != null ||
            circuitsIdentified != com.matiasdev.elecapp.features.inspections.domain.YesNoPartialUnknown.UNKNOWN ||
            neutralBarPresent != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            groundBarPresent != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            neutralAndGroundSeparated != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            improvisedConnections != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            mixedOrIncorrectColors != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            overheatingSigns != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            protectionCompatibility != com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility.NOT_ASSESSED ||
            !notes.isNullOrBlank()
    }

    private fun formatNumber(value: Double): String {
        val whole = value.toLong()
        return if (value == whole.toDouble()) whole.toString() else value.toString().replace(".", ",")
    }
}
