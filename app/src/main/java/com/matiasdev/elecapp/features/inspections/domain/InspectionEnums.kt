package com.matiasdev.elecapp.features.inspections.domain

enum class InspectionStatus {
    DRAFT,
    COMPLETED,
}

enum class InspectionType {
    VISUAL,
    VISUAL_AND_MEASUREMENTS,
}

enum class InspectionScope {
    VISUAL_INSPECTION,
    SECTOR_ASSESSMENT,
    GENERAL_ASSESSMENT,
}

enum class InspectionSectionReviewStatus {
    REVIEWED,
    NOT_APPLICABLE,
    NOT_VERIFIED,
}

enum class GeneralCondition {
    GOOD,
    FAIR,
    POOR,
    CRITICAL,
    NOT_ASSESSED,
}

enum class SupplyType {
    SINGLE_PHASE,
    THREE_PHASE,
    UNKNOWN,
}

enum class PropertyType {
    UNKNOWN,
    HOUSE,
    APARTMENT,
    COMMERCIAL,
    OTHER,
}

enum class AccessStatus {
    UNKNOWN,
    YES,
    NO,
    PARTIAL,
}

enum class YesNoUnknown {
    YES,
    NO,
    UNKNOWN,
}

enum class YesNoPartialUnknown {
    YES,
    NO,
    PARTIAL,
    UNKNOWN,
}

enum class ConductorMaterial {
    COPPER,
    ALUMINUM,
    UNKNOWN,
}

enum class ConductorCondition {
    GOOD,
    DETERIORATED,
    EXPOSED,
    OVERHEATED,
    NOT_ASSESSED,
}

enum class ProtectionCompatibility {
    COMPATIBLE,
    INCOMPATIBLE,
    REQUIRES_VERIFICATION,
    NOT_ASSESSED,
}

enum class DifferentialTestResult {
    PASSED,
    FAILED,
    NOT_TESTED,
    NOT_APPLICABLE,
}

enum class FindingCategory {
    PILLAR,
    MAIN_PANEL,
    GENERAL,
    OTHER,
}

enum class FindingSeverity {
    OK,
    RECOMMENDED,
    URGENT,
}

enum class UnverifiedItemType {
    GROUNDING_SYSTEM,
    INSULATION_RESISTANCE,
    HIDDEN_WIRING,
    INACCESSIBLE_AREA,
    PANEL_NOT_OPENED,
    PILLAR_NOT_ACCESSIBLE,
    CIRCUIT_OUT_OF_SERVICE,
    NO_MEASUREMENTS,
    INDIVIDUAL_CIRCUITS,
    OTHER,
}
