package com.matiasdev.elecapp.features.materials.domain

object MaterialListStatusActions {
    fun primaryTransition(status: MaterialListStatus): MaterialListStatus? = when (status) {
        MaterialListStatus.DRAFT -> MaterialListStatus.READY
        MaterialListStatus.READY -> MaterialListStatus.DELIVERED
        MaterialListStatus.DELIVERED -> MaterialListStatus.PURCHASED
        MaterialListStatus.PURCHASED,
        MaterialListStatus.CANCELLED,
        -> null
    }

    fun canCancel(status: MaterialListStatus): Boolean = status in setOf(
        MaterialListStatus.DRAFT,
        MaterialListStatus.READY,
        MaterialListStatus.DELIVERED,
    )
}
