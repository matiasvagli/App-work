package com.matiasdev.elecapp.features.agenda.ui

import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.matiasdev.elecapp.features.agenda.domain.visitVisualState
import com.matiasdev.elecapp.features.visits.domain.Visit

@Composable
fun VisitStatusChip(visit: Visit) {
    val visualState = visitVisualState(visit)
    AssistChip(
        onClick = {},
        label = { Text("${visit.status.label} · ${visualState.label}") },
    )
}
