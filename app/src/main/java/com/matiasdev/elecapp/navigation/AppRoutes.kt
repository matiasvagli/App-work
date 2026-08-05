package com.matiasdev.elecapp.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

object AppRoutes {
    const val HOME = "home"
    const val AGENDA = "agenda"
    const val SETTINGS = "settings"
    const val CLIENTS = "clients"
    const val INSPECTIONS = "inspections"
    const val QUOTES = "quotes"
    const val MATERIALS = "materials"
    const val ELECTRICAL_TOOLS = "tools/electrical"
    const val ELECTRICAL_TOOLS_POWER = "tools/electrical/power?clientId={clientId}&visitId={visitId}&inspectionId={inspectionId}&duplicateId={duplicateId}"
    const val ELECTRICAL_TOOLS_VOLTAGE_DROP = "tools/electrical/voltage-drop?clientId={clientId}&visitId={visitId}&inspectionId={inspectionId}&duplicateId={duplicateId}"
    const val ELECTRICAL_TOOLS_HISTORY = "tools/electrical/history"
    const val TECHNICAL_CALCULATION_DETAIL = "tools/electrical/calculations/{calculationId}"
    const val CLIENT_CREATE = "clients/new?notes={notes}&phone={phone}&source={source}"
    const val CLIENT_DETAIL = "clients/{clientId}"
    const val CLIENT_EDIT = "clients/{clientId}/edit"
    const val CLIENT_VISITS = "clients/{clientId}/visits"
    const val VISIT_CREATE = "visits/new?clientId={clientId}"
    const val VISIT_DETAIL = "visits/{visitId}"
    const val VISIT_EDIT = "visits/{visitId}/edit"
    const val INSPECTION_OVERVIEW = "inspections/{inspectionId}"
    const val INSPECTION_GENERAL = "inspections/{inspectionId}/general"
    const val INSPECTION_PILLAR = "inspections/{inspectionId}/pillar"
    const val INSPECTION_MAIN_PANEL = "inspections/{inspectionId}/main-panel"
    const val INSPECTION_FINDINGS = "inspections/{inspectionId}/findings"
    const val INSPECTION_UNVERIFIED = "inspections/{inspectionId}/unverified"
    const val INSPECTION_TECHNICAL_COMMENT = "inspections/{inspectionId}/technical-comment"
    const val INSPECTION_FINAL_REPORT = "inspections/{inspectionId}/final-report"
    const val QUOTE_CREATE = "quotes/new?clientId={clientId}&visitId={visitId}&inspectionId={inspectionId}"
    const val QUOTE_DETAIL = "quotes/{quoteId}"
    const val QUOTE_EDIT = "quotes/{quoteId}/edit"
    const val MATERIAL_CREATE = "materials/new?clientId={clientId}&visitId={visitId}&inspectionId={inspectionId}&quoteId={quoteId}"
    const val MATERIAL_DETAIL = "materials/{materialListId}"
    const val MATERIAL_EDIT = "materials/{materialListId}/edit"
    const val CLIENT_ID = "clientId"
    const val VISIT_ID = "visitId"
    const val INSPECTION_ID = "inspectionId"
    const val QUOTE_ID = "quoteId"
    const val MATERIAL_LIST_ID = "materialListId"
    const val CALCULATION_ID = "calculationId"
    const val DUPLICATE_ID = "duplicateId"
    const val NOTES = "notes"
    const val PHONE = "phone"
    const val SOURCE = "source"
    const val SOURCE_VISIT = "visit"
    const val SOURCE_SHARED_TEXT = "shared_text"
    const val CREATED_CLIENT_ID = "createdClientId"

    val clientIdArguments = listOf(
        navArgument(CLIENT_ID) { type = NavType.StringType },
    )

    val visitIdArguments = listOf(
        navArgument(VISIT_ID) { type = NavType.StringType },
    )

    val inspectionIdArguments = listOf(
        navArgument(INSPECTION_ID) { type = NavType.StringType },
    )

    val quoteIdArguments = listOf(navArgument(QUOTE_ID) { type = NavType.StringType })

    val materialListIdArguments = listOf(navArgument(MATERIAL_LIST_ID) { type = NavType.StringType })

    val technicalCalculationIdArguments = listOf(navArgument(CALCULATION_ID) { type = NavType.StringType })

    val clientCreateArguments = listOf(
        navArgument(NOTES) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        },
        navArgument(PHONE) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        },
        navArgument(SOURCE) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        },
    )

    val visitCreateArguments = listOf(
        navArgument(CLIENT_ID) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        },
    )

    val quoteCreateArguments = listOf(
        optionalStringArgument(CLIENT_ID),
        optionalStringArgument(VISIT_ID),
        optionalStringArgument(INSPECTION_ID),
    )

    val materialCreateArguments = listOf(
        optionalStringArgument(CLIENT_ID),
        optionalStringArgument(VISIT_ID),
        optionalStringArgument(INSPECTION_ID),
        optionalStringArgument(QUOTE_ID),
    )

    val electricalToolArguments = listOf(
        optionalStringArgument(CLIENT_ID),
        optionalStringArgument(VISIT_ID),
        optionalStringArgument(INSPECTION_ID),
        optionalStringArgument(DUPLICATE_ID),
    )

    fun clientCreate(notes: String? = null, phone: String? = null, source: String? = null): String {
        return "clients/new?notes=${notes.orEmpty()}&phone=${phone.orEmpty()}&source=${source.orEmpty()}"
    }

    fun clientDetail(clientId: String): String = "clients/$clientId"

    fun clientEdit(clientId: String): String = "clients/$clientId/edit"

    fun clientVisits(clientId: String): String = "clients/$clientId/visits"

    fun visitCreate(clientId: String? = null): String = "visits/new?clientId=${clientId.orEmpty()}"

    fun visitDetail(visitId: String): String = "visits/$visitId"

    fun visitEdit(visitId: String): String = "visits/$visitId/edit"

    fun inspectionOverview(inspectionId: String): String = "inspections/$inspectionId"

    fun inspectionGeneral(inspectionId: String): String = "inspections/$inspectionId/general"

    fun inspectionPillar(inspectionId: String): String = "inspections/$inspectionId/pillar"

    fun inspectionMainPanel(inspectionId: String): String = "inspections/$inspectionId/main-panel"

    fun inspectionFindings(inspectionId: String): String = "inspections/$inspectionId/findings"

    fun inspectionUnverified(inspectionId: String): String = "inspections/$inspectionId/unverified"

    fun inspectionTechnicalComment(inspectionId: String): String = "inspections/$inspectionId/technical-comment"

    fun inspectionFinalReport(inspectionId: String): String = "inspections/$inspectionId/final-report"

    fun quoteCreate(clientId: String? = null, visitId: String? = null, inspectionId: String? = null): String {
        return "quotes/new?clientId=${clientId.orEmpty()}&visitId=${visitId.orEmpty()}&inspectionId=${inspectionId.orEmpty()}"
    }

    fun quoteDetail(quoteId: String): String = "quotes/$quoteId"

    fun quoteEdit(quoteId: String): String = "quotes/$quoteId/edit"

    fun materialCreate(
        clientId: String? = null,
        visitId: String? = null,
        inspectionId: String? = null,
        quoteId: String? = null,
    ): String {
        return "materials/new?clientId=${clientId.orEmpty()}&visitId=${visitId.orEmpty()}&inspectionId=${inspectionId.orEmpty()}&quoteId=${quoteId.orEmpty()}"
    }

    fun materialDetail(materialListId: String): String = "materials/$materialListId"

    fun materialEdit(materialListId: String): String = "materials/$materialListId/edit"

    fun electricalToolsPower(clientId: String? = null, visitId: String? = null, inspectionId: String? = null, duplicateId: String? = null): String {
        return "tools/electrical/power?clientId=${clientId.orEmpty()}&visitId=${visitId.orEmpty()}&inspectionId=${inspectionId.orEmpty()}&duplicateId=${duplicateId.orEmpty()}"
    }

    fun electricalToolsVoltageDrop(clientId: String? = null, visitId: String? = null, inspectionId: String? = null, duplicateId: String? = null): String {
        return "tools/electrical/voltage-drop?clientId=${clientId.orEmpty()}&visitId=${visitId.orEmpty()}&inspectionId=${inspectionId.orEmpty()}&duplicateId=${duplicateId.orEmpty()}"
    }

    fun technicalCalculationDetail(calculationId: String): String = "tools/electrical/calculations/$calculationId"

    private fun optionalStringArgument(name: String) = navArgument(name) {
        type = NavType.StringType
        nullable = true
        defaultValue = null
    }
}
