package com.matiasdev.elecapp.features.materials.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MaterialListStatusActionsTest {
    @Test
    fun primaryTransitionsFollowDocumentFlow() {
        assertEquals(MaterialListStatus.READY, MaterialListStatusActions.primaryTransition(MaterialListStatus.DRAFT))
        assertEquals(MaterialListStatus.DELIVERED, MaterialListStatusActions.primaryTransition(MaterialListStatus.READY))
        assertEquals(MaterialListStatus.PURCHASED, MaterialListStatusActions.primaryTransition(MaterialListStatus.DELIVERED))
        assertNull(MaterialListStatusActions.primaryTransition(MaterialListStatus.PURCHASED))
        assertNull(MaterialListStatusActions.primaryTransition(MaterialListStatus.CANCELLED))
    }

    @Test
    fun cancelIsOnlyAvailableBeforeFinalStates() {
        assertTrue(MaterialListStatusActions.canCancel(MaterialListStatus.DRAFT))
        assertTrue(MaterialListStatusActions.canCancel(MaterialListStatus.READY))
        assertTrue(MaterialListStatusActions.canCancel(MaterialListStatus.DELIVERED))
        assertFalse(MaterialListStatusActions.canCancel(MaterialListStatus.PURCHASED))
        assertFalse(MaterialListStatusActions.canCancel(MaterialListStatus.CANCELLED))
    }
}
