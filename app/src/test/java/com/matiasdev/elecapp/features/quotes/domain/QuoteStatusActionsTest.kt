package com.matiasdev.elecapp.features.quotes.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteStatusActionsTest {
    @Test
    fun primaryTransitionsOnlyExposeValidNextActions() {
        assertEquals(listOf(QuoteStatus.READY), QuoteStatusActions.primaryTransitions(QuoteStatus.DRAFT))
        assertEquals(listOf(QuoteStatus.SENT), QuoteStatusActions.primaryTransitions(QuoteStatus.READY))
        assertEquals(listOf(QuoteStatus.APPROVED, QuoteStatus.REJECTED), QuoteStatusActions.primaryTransitions(QuoteStatus.SENT))
        assertEquals(emptyList<QuoteStatus>(), QuoteStatusActions.primaryTransitions(QuoteStatus.APPROVED))
        assertEquals(emptyList<QuoteStatus>(), QuoteStatusActions.primaryTransitions(QuoteStatus.REJECTED))
        assertEquals(emptyList<QuoteStatus>(), QuoteStatusActions.primaryTransitions(QuoteStatus.CANCELLED))
    }

    @Test
    fun cancelIsSecondaryAndUnavailableAfterClosedStates() {
        assertTrue(QuoteStatusActions.canCancel(QuoteStatus.DRAFT))
        assertTrue(QuoteStatusActions.canCancel(QuoteStatus.READY))
        assertTrue(QuoteStatusActions.canCancel(QuoteStatus.SENT))
        assertFalse(QuoteStatusActions.canCancel(QuoteStatus.APPROVED))
        assertFalse(QuoteStatusActions.canCancel(QuoteStatus.REJECTED))
        assertFalse(QuoteStatusActions.canCancel(QuoteStatus.CANCELLED))
    }
}
