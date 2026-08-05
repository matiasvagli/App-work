package com.matiasdev.elecapp.features.visits.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class VisitStatusTransitionsTest {
    @Test
    fun `completed visits do not transition back accidentally`() {
        assertEquals(emptyList<VisitStatus>(), VisitStatusTransitions.allowedNextStatuses(VisitStatus.COMPLETED))
    }

    @Test
    fun `confirmed visits can be started or cancelled`() {
        assertEquals(
            listOf(VisitStatus.IN_PROGRESS, VisitStatus.CANCELLED),
            VisitStatusTransitions.allowedNextStatuses(VisitStatus.CONFIRMED),
        )
    }

    @Test
    fun `in progress visits can be completed`() {
        assertEquals(listOf(VisitStatus.COMPLETED), VisitStatusTransitions.allowedNextStatuses(VisitStatus.IN_PROGRESS))
    }
}
