package com.matiasdev.elecapp.features.inspections.summary

import com.matiasdev.elecapp.features.inspections.domain.ElectricalInspection
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.summary.TechnicalCalculationTextGenerator
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.FindingReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.FindingSourceType
import com.matiasdev.elecapp.features.inspections.domain.InspectionAggregate
import com.matiasdev.elecapp.features.inspections.domain.InspectionFindingProposalBuilder
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.MainPanelInspection
import com.matiasdev.elecapp.features.inspections.domain.MainPanelCircuit
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurement
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementSection
import com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin
import com.matiasdev.elecapp.features.inspections.domain.PillarMeasurement
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
        val aggregateWithFindings = InspectionFindingProposalBuilder.mergeIntoAggregate(aggregate)
        val inspection = aggregateWithFindings.inspection
        if (inspection.scope == InspectionScope.VISUAL_INSPECTION) {
            return generateVisualInspection(aggregateWithFindings, visit, zoneId, calculations)
        }
        return buildString {
            appendHeader(inspection, visit, zoneId)
            appendLine()
            appendLine("TIPO DE RELEVAMIENTO")
            appendLine(inspection.inspectionType.label())
            appendPillar(inspection, aggregateWithFindings.pillar, aggregateWithFindings.pillarMeasurements)
            appendMainPanel(aggregateWithFindings.mainPanel, aggregateWithFindings.mainPanelMeasurements, aggregateWithFindings.mainPanelCircuits)
            appendCalculations(calculations)
            appendFindings(aggregateWithFindings)
            appendUnverified(aggregateWithFindings)
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
            appendVisualPillar(inspection, aggregate.pillar, aggregate.pillarMeasurements)
            appendVisualMainPanel(aggregate.mainPanel, aggregate.mainPanelMeasurements, aggregate.mainPanelCircuits)
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

    private fun StringBuilder.appendPillar(inspection: ElectricalInspection, pillar: PillarInspection?, measurements: List<PillarMeasurement>) {
        appendLine()
        appendLine("PILAR Y ACOMETIDA")
        if (pillar == null) {
            appendLine("- No evaluado")
            return
        }
        if (pillar.reviewStatus != InspectionSectionReviewStatus.REVIEWED) {
            appendLine("- Estado de la sección: ${pillar.reviewStatus.label()}")
            appendLineIfNotBlank("- Observación", pillar.notes)
            return
        }
        appendLine("- Tipo de inmueble: ${pillar.propertyTypeLabel(inspection)}")
        appendLine("- Suministro: ${(pillar.supplyType ?: inspection.supplyType).label()}")
        appendLine("- Accesibilidad: ${pillar.accessible.label().lowercase()}")
        appendCondition("- Estado general", pillar.generalCondition)
        appendProtection("- Térmica principal", pillar.mainBreakerPresent, pillar.mainBreakerAmps ?: pillar.mainBreakerOtherAmps, "A")
        appendPillarDifferential(pillar)
        appendPillarMeasurements(measurements)
        appendPillarConductor(pillar)
        appendLine("- Compatibilidad protección/conductor: ${pillar.protectionCompatibility.label().lowercase()}")
        appendLineIfNotBlank("- Observación de compatibilidad", pillar.protectionCompatibilityNotes)
        appendLineIfNotBlank("- Observación", pillar.notes)
    }

    private fun StringBuilder.appendVisualPillar(inspection: ElectricalInspection, pillar: PillarInspection?, measurements: List<PillarMeasurement>) {
        if (pillar == null || !pillar.hasVisualContent()) return
        appendLine()
        appendLine("PILAR Y ACOMETIDA")
        if (pillar.reviewStatus != InspectionSectionReviewStatus.REVIEWED) {
            appendLine("- Estado de revisión: ${pillar.reviewStatus.label()}")
            return
        }
        appendLine("- Tipo de inmueble: ${pillar.propertyTypeLabel(inspection)}")
        if ((pillar.supplyType ?: inspection.supplyType) != com.matiasdev.elecapp.features.inspections.domain.SupplyType.UNKNOWN) {
            appendLine("- Suministro: ${(pillar.supplyType ?: inspection.supplyType).label()}")
        }
        appendVisualAccess("- Accesibilidad", pillar.accessible)
        appendVisualCondition("- Estado general", pillar.generalCondition)
        appendProtection("- Térmica principal visible", pillar.mainBreakerPresent, pillar.mainBreakerAmps ?: pillar.mainBreakerOtherAmps, "A")
        appendPillarDifferential(pillar)
        appendPillarMeasurements(measurements)
        appendPillarConductor(pillar)
        appendVisualEnum("- Compatibilidad protección/conductor", pillar.protectionCompatibility, com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility.NOT_ASSESSED)
        appendLineIfNotBlank("- Observación de compatibilidad", pillar.protectionCompatibilityNotes)
        appendLineIfNotBlank("- Observaciones", pillar.notes)
    }

    private fun StringBuilder.appendMainPanel(panel: MainPanelInspection?, measurements: List<MainPanelMeasurement>, circuits: List<MainPanelCircuit>) {
        appendLine()
        appendLine("TABLERO PRINCIPAL")
        if (panel == null) {
            appendLine("- No evaluado")
            return
        }
        if (panel.reviewStatus != InspectionSectionReviewStatus.REVIEWED) {
            appendLine("- Estado de la sección: ${panel.reviewStatus.label()}")
            appendLineIfNotBlank("- Observación", panel.notes)
            return
        }
        appendLine("- Accesibilidad: ${panel.accessible.label().lowercase()}")
        appendCondition("- Estado general", panel.generalCondition)
        appendMainPanelMeasurements("Tensión de entrada", measurements.filter { it.section == MainPanelMeasurementSection.INPUT_VOLTAGE })
        appendMainPanelDifferential(panel)
        panel.circuitCount?.let { appendLine("- Cantidad de circuitos: $it") }
        appendMainPanelCircuits(circuits)
        appendLine("- Colores de conductores: ${panel.conductorColorStatus.label().lowercase()}")
        appendLine("- Bornera de neutro: ${panel.neutralBarPresent.label().lowercase()}")
        appendLine("- Bornera de tierra: ${panel.groundBarPresent.label().lowercase()}")
        appendLine("- Neutro y tierra separados: ${panel.neutralAndGroundSeparated.label().lowercase()}")
        appendLine("- Conductores de protección presentes: ${panel.protectionConductorsPresent.label().lowercase()}")
        appendLine("- Empalmes improvisados: ${panel.improvisedConnections.label().lowercase()}")
        appendLine("- Signos de recalentamiento: ${panel.overheatingSigns.label().lowercase()}")
        appendLine("- Partes expuestas o aislación dañada: ${panel.exposedPartsOrDamagedInsulation.label().lowercase()}")
        appendLine("- Compatibilidad protección/conductor: ${panel.protectionCompatibility.label().lowercase()}")
        appendLineIfNotBlank("- Observación de cableado y riesgos", panel.wiringRisksNotes)
        appendProtectionConductorCheck(panel, measurements.filter { it.section == MainPanelMeasurementSection.PROTECTION_CONDUCTOR_CHECK })
        appendLineIfNotBlank("- Observación", panel.notes)
    }

    private fun StringBuilder.appendVisualMainPanel(panel: MainPanelInspection?, measurements: List<MainPanelMeasurement>, circuits: List<MainPanelCircuit>) {
        if (panel == null || !panel.hasVisualContent()) return
        appendLine()
        appendLine("TABLERO PRINCIPAL")
        if (panel.reviewStatus != InspectionSectionReviewStatus.REVIEWED) {
            appendLine("- Estado de revisión: ${panel.reviewStatus.label()}")
            return
        }
        appendVisualAccess("- Accesibilidad", panel.accessible)
        appendVisualCondition("- Estado general", panel.generalCondition)
        appendMainPanelMeasurements("Tensión de entrada", measurements.filter { it.section == MainPanelMeasurementSection.INPUT_VOLTAGE })
        appendMainPanelDifferential(panel)
        panel.circuitCount?.let { appendLine("- Cantidad visible de circuitos: $it") }
        appendMainPanelCircuits(circuits)
        if (panel.conductorColorStatus != com.matiasdev.elecapp.features.inspections.domain.ConductorColorStatus.UNKNOWN) {
            appendLine("- Colores visibles: ${panel.conductorColorStatus.label()}")
        }
        appendVisualYesNo("- Bornera de neutro visible", panel.neutralBarPresent)
        appendVisualYesNo("- Bornera de tierra visible", panel.groundBarPresent)
        appendVisualYesNo("- Neutro y tierra aparentemente separados", panel.neutralAndGroundSeparated)
        appendVisualYesNoPartial("- Conductores de protección presentes", panel.protectionConductorsPresent)
        appendVisualYesNo("- Empalmes improvisados visibles", panel.improvisedConnections)
        appendVisualYesNo("- Signos visibles de recalentamiento", panel.overheatingSigns)
        appendVisualYesNo("- Partes expuestas o aislación dañada", panel.exposedPartsOrDamagedInsulation)
        appendVisualEnum("- Compatibilidad protección/conductor", panel.protectionCompatibility, com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility.NOT_ASSESSED)
        appendLineIfNotBlank("- Observación de cableado y riesgos", panel.wiringRisksNotes)
        appendProtectionConductorCheck(panel, measurements.filter { it.section == MainPanelMeasurementSection.PROTECTION_CONDUCTOR_CHECK })
        appendLineIfNotBlank("- Observaciones", panel.notes)
    }

    private fun StringBuilder.appendFindings(aggregate: InspectionAggregate) {
        appendLine()
        appendLine("HALLAZGOS")
        val reportFindings = aggregate.findings.filter { it.includeInReport && it.sourceType != FindingSourceType.NOT_VERIFIED && it.sourceType != FindingSourceType.DATA_REVIEW }
        if (reportFindings.isEmpty()) {
            appendLine("- Sin hallazgos registrados")
            return
        }
        reportFindings.forEach { finding ->
            appendLine("[${finding.severity.label().uppercase()}] ${finding.title}")
            appendLine("Categoría: ${finding.category.label()}")
            val prefix = if (finding.sourceType == FindingSourceType.RULE_SUGGESTION && finding.reviewStatus != FindingReviewStatus.CONFIRMED) {
                "Sugerencia de la app, requiere validación del técnico: "
            } else {
                ""
            }
            appendLine("Descripción: $prefix${finding.description}")
            appendLineIfNotBlank("Recomendación", finding.recommendation)
            appendLineIfNotBlank("Observación", finding.technicianNotes)
            appendLine()
        }
    }

    private fun StringBuilder.appendVisualFindings(aggregate: InspectionAggregate) {
        val reportFindings = aggregate.findings.filter { it.includeInReport && it.sourceType != FindingSourceType.NOT_VERIFIED && it.sourceType != FindingSourceType.DATA_REVIEW }
        if (reportFindings.isEmpty()) return
        appendLine()
        appendLine("HALLAZGOS")
        reportFindings.forEach { finding ->
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
        val findingItems = aggregate.findings.filter { it.includeInReport && it.sourceType == FindingSourceType.NOT_VERIFIED }
        if (aggregate.unverifiedItems.isEmpty() && findingItems.isEmpty()) {
            appendLine("- Sin elementos registrados")
            return
        }
        aggregate.unverifiedItems.forEach { item ->
            val suffix = item.description?.takeIf(String::isNotBlank)?.let { ": $it" }.orEmpty()
            appendLine("- ${item.type.label()}$suffix")
        }
        findingItems.forEach { appendLine("- ${it.description}") }
    }

    private fun StringBuilder.appendVisualUnverified(aggregate: InspectionAggregate) {
        val findingItems = aggregate.findings.filter { it.includeInReport && it.sourceType == FindingSourceType.NOT_VERIFIED }
        if (aggregate.unverifiedItems.isEmpty() && findingItems.isEmpty()) return
        appendLine()
        appendLine("NO VERIFICADO")
        aggregate.unverifiedItems.forEach { item ->
            val suffix = item.description?.takeIf(String::isNotBlank)?.let { ": $it" }.orEmpty()
            appendLine("- ${item.type.label()}$suffix")
        }
        findingItems.forEach { appendLine("- ${it.description}") }
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
            propertyType != null ||
            supplyType != null ||
            accessible != com.matiasdev.elecapp.features.inspections.domain.AccessStatus.UNKNOWN ||
            generalCondition != GeneralCondition.NOT_ASSESSED ||
            mainBreakerPresent != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            mainBreakerAmps != null ||
            mainBreakerOtherAmps != null ||
            differentialPresent != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            differentialRatedAmps != null ||
            differentialSensitivityMa != null ||
            conductorSectionMm2 != null ||
            conductorOtherSectionMm2 != null ||
            conductorMaterial != com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial.UNKNOWN ||
            conductorCondition != com.matiasdev.elecapp.features.inspections.domain.ConductorCondition.NOT_ASSESSED ||
            protectionCompatibility != com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility.NOT_ASSESSED ||
            !protectionCompatibilityNotes.isNullOrBlank() ||
            !notes.isNullOrBlank()
    }

    private fun MainPanelInspection.hasVisualContent(): Boolean {
        return reviewStatus != InspectionSectionReviewStatus.REVIEWED ||
            accessible != com.matiasdev.elecapp.features.inspections.domain.AccessStatus.UNKNOWN ||
            generalCondition != GeneralCondition.NOT_ASSESSED ||
            differentialPresent != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            differentialRatedAmps != null ||
            differentialOtherRatedAmps != null ||
            differentialSensitivityMa != null ||
            differentialOtherSensitivityMa != null ||
            differentialTestResult != com.matiasdev.elecapp.features.inspections.domain.DifferentialTestResult.NOT_TESTED ||
            circuitCount != null ||
            circuitsIdentified != com.matiasdev.elecapp.features.inspections.domain.YesNoPartialUnknown.UNKNOWN ||
            neutralBarPresent != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            groundBarPresent != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            neutralAndGroundSeparated != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            protectionConductorsPresent != com.matiasdev.elecapp.features.inspections.domain.YesNoPartialUnknown.UNKNOWN ||
            improvisedConnections != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            conductorColorStatus != com.matiasdev.elecapp.features.inspections.domain.ConductorColorStatus.UNKNOWN ||
            overheatingSigns != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            exposedPartsOrDamagedInsulation != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN ||
            protectionCompatibility != com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility.NOT_ASSESSED ||
            !wiringRisksNotes.isNullOrBlank() ||
            protectionConductorCheckResult != com.matiasdev.elecapp.features.inspections.domain.ProtectionConductorCheckResult.NOT_VERIFIED ||
            !notes.isNullOrBlank()
    }

    private fun StringBuilder.appendProtection(
        label: String,
        present: com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown,
        value: Int?,
        unit: String,
    ) {
        when {
            present == com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.YES && value != null -> appendLine("$label: $value $unit")
            present != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN -> appendLine("$label: ${present.label().lowercase()}")
        }
    }

    private fun StringBuilder.appendPillarDifferential(pillar: PillarInspection) {
        val present = pillar.differentialPresent
        if (present == com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN) return
        if (present != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.YES) {
            appendLine("- Interruptor diferencial en pilar: ${present.label().lowercase()}")
            return
        }
        val details = buildList {
            (pillar.differentialRatedAmps ?: pillar.differentialOtherRatedAmps)?.let { add("$it A") }
            (pillar.differentialSensitivityMa ?: pillar.differentialOtherSensitivityMa)?.let { add("$it mA") }
            add("prueba manual: ${pillar.differentialTestResult.label().lowercase()}")
        }
        appendLine("- Interruptor diferencial en pilar: ${details.joinToString(", ")}")
    }

    private fun StringBuilder.appendPillarMeasurements(measurements: List<PillarMeasurement>) {
        val active = measurements
            .filterNot { it.isDeleted }
            .filter { it.value != null || it.origin == MeasurementOrigin.NOT_VERIFIED }
            .sortedWith(compareBy({ it.sortOrder }, { it.createdAt }))
        if (active.isEmpty()) return
        appendLine("- Mediciones registradas:")
        active.forEach { measurement ->
            val value = measurement.value?.let { "${formatNumber(it)} ${measurement.unit}" } ?: "no verificado"
            appendLine("  - ${measurement.type.label()}: $value (${measurement.origin.label().lowercase()})")
        }
    }

    private fun StringBuilder.appendPillarConductor(pillar: PillarInspection) {
        val section = pillar.conductorSectionMm2 ?: pillar.conductorOtherSectionMm2
        val material = when {
            pillar.conductorMaterial == com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial.OTHER && !pillar.conductorMaterialOther.isNullOrBlank() -> pillar.conductorMaterialOther
            pillar.conductorMaterial != com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial.UNKNOWN -> pillar.conductorMaterial.label().lowercase()
            else -> null
        }
        if (section != null || material != null) {
            appendLine("- Conductores observados: ${listOfNotNull(section?.let { "${formatNumber(it)} mm²" }, material).joinToString(", ")}")
        }
        if (pillar.conductorCondition != com.matiasdev.elecapp.features.inspections.domain.ConductorCondition.NOT_ASSESSED) {
            appendLine("- Estado de conductores: ${pillar.conductorCondition.label().lowercase()}")
        }
    }

    private fun StringBuilder.appendMainPanelDifferential(panel: MainPanelInspection) {
        val present = panel.differentialPresent
        if (present == com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN) return
        if (present != com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.YES) {
            appendLine("- Interruptor diferencial: ${present.label().lowercase()}")
            return
        }
        val details = buildList {
            (panel.differentialRatedAmps ?: panel.differentialOtherRatedAmps)?.takeIf { it > 0 }?.let { add("$it A") }
            (panel.differentialSensitivityMa ?: panel.differentialOtherSensitivityMa)?.takeIf { it > 0 }?.let { add("$it mA") }
            add("prueba ${panel.differentialTestResult.label().lowercase()}")
        }
        appendLine("- Interruptor diferencial: ${details.joinToString(" / ")}")
    }

    private fun StringBuilder.appendMainPanelMeasurements(title: String, measurements: List<MainPanelMeasurement>) {
        val active = measurements.filterNot { it.isDeleted }.filter { it.value != null || it.origin == MeasurementOrigin.NOT_VERIFIED }
        if (active.isEmpty()) return
        appendLine("- $title:")
        active.sortedWith(compareBy({ it.sortOrder }, { it.createdAt })).forEach {
            val value = it.value?.let { measured -> "${formatNumber(measured)} ${it.unit}" } ?: "no verificado"
            appendLine("  - ${it.type.label()}: $value (${it.origin.label().lowercase()})")
        }
    }

    private fun StringBuilder.appendMainPanelCircuits(circuits: List<MainPanelCircuit>) {
        val active = circuits.filterNot { it.isDeleted }.filter { it.hasContent() }
        if (active.isEmpty()) return
        appendLine("- Circuitos:")
        active.sortedWith(compareBy({ it.sortOrder }, { it.createdAt })).forEachIndexed { index, circuit ->
            appendLine("  - ${circuit.summary(index)}")
        }
    }

    private fun StringBuilder.appendProtectionConductorCheck(panel: MainPanelInspection, measurements: List<MainPanelMeasurement>) {
        val hasMeasurements = measurements.any { !it.isDeleted && (it.value != null || it.origin == MeasurementOrigin.NOT_VERIFIED) }
        if (!hasMeasurements && panel.protectionConductorCheckResult == com.matiasdev.elecapp.features.inspections.domain.ProtectionConductorCheckResult.NOT_VERIFIED) return
        appendLine("- Verificación rápida del conductor de protección:")
        appendMainPanelMeasurements("Valores registrados", measurements)
        appendLine("  Resultado orientativo: ${panel.protectionConductorCheckResult.label().lowercase()}")
    }

    private fun MainPanelCircuit.hasContent(): Boolean {
        return destination != com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.UNIDENTIFIED ||
            !destinationOther.isNullOrBlank() ||
            breakerAmps != null ||
            breakerOtherAmps?.let { it > 0 } == true ||
            breakerCurve != com.matiasdev.elecapp.features.inspections.domain.BreakerCurve.UNKNOWN ||
            conductorSectionMm2 != null ||
            conductorOtherSectionMm2?.let { it > 0.0 } == true ||
            conductorMaterial != com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial.UNKNOWN ||
            consumptionAmps != null ||
            consumptionOrigin != MeasurementOrigin.NOT_VERIFIED ||
            !notes.isNullOrBlank()
    }

    private fun MainPanelCircuit.summary(index: Int): String {
        val destinationText = when {
            destination == com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.OTHER && !destinationOther.isNullOrBlank() -> destinationOther
            destination == com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.UNIDENTIFIED -> "Circuito ${index + 1} sin identificar"
            else -> "Circuito ${destination.label().lowercase()}"
        }
        val details = buildList {
            (breakerAmps ?: breakerOtherAmps?.takeIf { it > 0 })?.let { add("térmica $it A") }
            if (breakerCurve != com.matiasdev.elecapp.features.inspections.domain.BreakerCurve.UNKNOWN) add("curva ${breakerCurve.label()}")
            val section = conductorSectionMm2 ?: conductorOtherSectionMm2?.takeIf { it > 0.0 }
            val material = when {
                conductorMaterial == com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial.OTHER && !conductorMaterialOther.isNullOrBlank() -> conductorMaterialOther
                conductorMaterial != com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial.UNKNOWN -> conductorMaterial.label().lowercase()
                else -> null
            }
            if (section != null || material != null) add("conductor ${listOfNotNull(material, section?.let { "${formatNumber(it)} mm²" }).joinToString(" ")}")
            consumptionAmps?.let { add("consumo ${consumptionOrigin.label().lowercase()} ${formatNumber(it)} A") }
            notes?.takeIf(String::isNotBlank)?.let { add(it) }
        }
        return if (details.isEmpty()) destinationText else "$destinationText: ${details.joinToString(", ")}"
    }

    private fun PillarInspection.propertyTypeLabel(inspection: ElectricalInspection): String {
        val type = propertyType ?: inspection.propertyType
        return if (type == com.matiasdev.elecapp.features.inspections.domain.PropertyType.OTHER && !propertyTypeOther.isNullOrBlank()) {
            "Otro: $propertyTypeOther"
        } else {
            type.label()
        }
    }

    private fun formatNumber(value: Double): String {
        val whole = value.toLong()
        return if (value == whole.toDouble()) whole.toString() else value.toString().replace(".", ",")
    }
}
