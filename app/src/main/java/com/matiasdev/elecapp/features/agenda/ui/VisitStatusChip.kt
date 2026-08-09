package com.matiasdev.elecapp.features.agenda.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.features.agenda.domain.VisitVisualState
import com.matiasdev.elecapp.features.agenda.domain.visitVisualState
import com.matiasdev.elecapp.features.visits.domain.Visit

@Composable
fun VisitStatusChip(visit: Visit, modifier: Modifier = Modifier) {
    val visualState = visitVisualState(visit)
    val (containerColor, contentColor) = when (visualState) {
        VisitVisualState.COMPLETED -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
        VisitVisualState.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        VisitVisualState.SOON -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        VisitVisualState.PAST_PENDING -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        VisitVisualState.UPCOMING -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        VisitVisualState.CANCELLED -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
    ) {
        Text(
            text = visualState.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
