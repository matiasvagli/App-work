package com.matiasdev.elecapp.features.inspections.domain

import com.matiasdev.elecapp.features.electricalrules.domain.ConductorAmpacityReference
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfig
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.summary.TechnicalValueFormatter

data class ProtectionLoadEvaluation(
    val measuredCurrentAmps: Double,
    val breakerAmps: Int,
    val loadPercent: Double,
    val status: ProtectionLoadStatus,
    val classification: TechnicalClassification,
) {
    val label: String = status.label
}

enum class ProtectionLoadStatus(val label: String) {
    ACCEPTABLE("aceptable"),
    ELEVATED_LOAD("carga elevada"),
    NEAR_LIMIT("próximo al límite"),
    REQUIRES_REVIEW("requiere revisión"),
}

object ProtectionLoadEvaluator {
    private const val ELEVATED_LOAD_PERCENT = 80.0
    private const val NEAR_LIMIT_PERCENT = 95.0
    private const val REQUIRES_REVIEW_PERCENT = 100.0

    fun evaluate(measuredCurrentAmps: Double?, breakerAmps: Int?): ProtectionLoadEvaluation? {
        val measured = measuredCurrentAmps?.takeIf { it.isFinite() && it >= 0.0 } ?: return null
        val breaker = breakerAmps?.takeIf { it > 0 } ?: return null
        val percent = measured / breaker * 100.0
        val status = when {
            percent < ELEVATED_LOAD_PERCENT -> ProtectionLoadStatus.ACCEPTABLE
            percent < NEAR_LIMIT_PERCENT -> ProtectionLoadStatus.ELEVATED_LOAD
            percent < REQUIRES_REVIEW_PERCENT -> ProtectionLoadStatus.NEAR_LIMIT
            else -> ProtectionLoadStatus.REQUIRES_REVIEW
        }
        return ProtectionLoadEvaluation(
            measuredCurrentAmps = measured,
            breakerAmps = breaker,
            loadPercent = percent,
            status = status,
            classification = when (status) {
                ProtectionLoadStatus.ACCEPTABLE -> TechnicalClassification.ACCEPTABLE
                ProtectionLoadStatus.ELEVATED_LOAD,
                ProtectionLoadStatus.NEAR_LIMIT,
                ProtectionLoadStatus.REQUIRES_REVIEW -> TechnicalClassification.REQUIRES_REVIEW
            },
        )
    }
}

data class ProtectionConductorCompatibilityEvaluation(
    val breakerAmps: Int,
    val sectionMm2: Double,
    val material: ConductorMaterial,
    val observedSectionReferenceAmps: Double?,
    val sectionReferenceForBreaker: ConductorAmpacityReference.Reference?,
    val classification: TechnicalClassification,
)

object ProtectionConductorCompatibilityEvaluator {
    fun evaluate(
        breakerAmps: Int?,
        sectionMm2: Double?,
        material: ConductorMaterial,
        rules: List<ElectricalRuleConfig>,
    ): ProtectionConductorCompatibilityEvaluation? {
        val breaker = breakerAmps?.takeIf { it > 0 } ?: return null
        val section = sectionMm2?.takeIf { it.isFinite() && it > 0.0 } ?: return null
        val observedReference = when (material) {
            ConductorMaterial.COPPER -> ConductorAmpacityReference.maximumCopperAmps(section, rules)
            else -> null
        }
        val compatibleSection = when (material) {
            ConductorMaterial.COPPER -> ConductorAmpacityReference.minimumCopperSectionForAmps(breaker.toDouble(), rules)
            else -> null
        }
        val classification = when {
            observedReference == null -> TechnicalClassification.NOT_CLASSIFIED
            breaker <= observedReference -> TechnicalClassification.ACCEPTABLE
            else -> TechnicalClassification.CRITICAL_REVIEW
        }
        return ProtectionConductorCompatibilityEvaluation(
            breakerAmps = breaker,
            sectionMm2 = section,
            material = material,
            observedSectionReferenceAmps = observedReference,
            sectionReferenceForBreaker = compatibleSection,
            classification = classification,
        )
    }
}

fun ProtectionLoadEvaluation.detailText(): String {
    val percentText = TechnicalValueFormatter.withUnit(loadPercent, "%", 0)
    return when (status) {
        ProtectionLoadStatus.ACCEPTABLE,
        ProtectionLoadStatus.ELEVATED_LOAD -> "Carga medida equivalente al $percentText de la corriente nominal de la protección."
        ProtectionLoadStatus.NEAR_LIMIT -> "La corriente medida se encuentra próxima al valor nominal de la protección."
        ProtectionLoadStatus.REQUIRES_REVIEW -> "El consumo medido alcanza la corriente nominal de la protección. Se recomienda verificar demanda y condiciones de funcionamiento del circuito."
    }
}

fun ProtectionConductorCompatibilityEvaluation.primaryResultText(): String = when (classification) {
    TechnicalClassification.ACCEPTABLE -> "aceptable"
    TechnicalClassification.REQUIRES_REVIEW -> "requiere revisión"
    TechnicalClassification.CRITICAL_REVIEW -> "crítico"
    TechnicalClassification.INFORMATIONAL -> "informativo"
    TechnicalClassification.NOT_CLASSIFIED -> "sin clasificar"
}

fun ProtectionConductorCompatibilityEvaluation.detailText(): String {
    val breakerText = TechnicalValueFormatter.withUnit(breakerAmps.toDouble(), "A", 0)
    val sectionText = TechnicalValueFormatter.withUnit(sectionMm2, "mm²")
    val conductorText = when (material) {
        ConductorMaterial.COPPER -> "conductor observado de cobre de $sectionText"
        ConductorMaterial.ALUMINUM -> "conductor observado de aluminio de $sectionText"
        ConductorMaterial.OTHER -> "conductor observado de $sectionText"
        ConductorMaterial.UNKNOWN -> "conductor observado de $sectionText"
    }
    if (classification == TechnicalClassification.ACCEPTABLE) {
        return "Protección de $breakerText asociada a $conductorText. Compatibilidad orientativa aceptable según las reglas configuradas."
    }

    val base = "Protección de $breakerText asociada a $conductorText. La combinación requiere revisión. " +
        "Verificar sección real, método de instalación, capacidad de conducción y demanda antes de definir la corrección."
    if (classification == TechnicalClassification.NOT_CLASSIFIED) return base

    val references = buildList {
        observedSectionReferenceAmps?.let {
            add("* $sectionText -> protección de referencia ${TechnicalValueFormatter.withUnit(it, "A", 0)}.")
        }
        val compatibleSection = sectionReferenceForBreaker
        if (compatibleSection != null) {
            add("* Para $breakerText -> sección de referencia ${TechnicalValueFormatter.withUnit(compatibleSection.sectionMm2, "mm²")}.")
        } else {
            add("* Para mantener la protección existente, verificar y dimensionar una sección de conductor compatible según las condiciones reales de instalación.")
        }
    }
    return "$base\nReferencia orientativa según reglas configuradas:\n${references.joinToString("\n")}"
}
