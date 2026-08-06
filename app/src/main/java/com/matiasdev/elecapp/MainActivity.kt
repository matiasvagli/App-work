package com.matiasdev.elecapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.matiasdev.elecapp.core.external.extractPlainSharedText
import com.matiasdev.elecapp.core.ui.theme.ElecAppTheme
import com.matiasdev.elecapp.features.reminders.scheduling.EXTRA_VISIT_ID
import com.matiasdev.elecapp.navigation.ElecNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as ElecApplication).appContainer
        val sharedText = extractPlainSharedText(intent)
        val initialVisitId = intent.getStringExtra(EXTRA_VISIT_ID)
        intent = android.content.Intent()

        setContent {
            ElecAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ElecNavHost(
                        clientRepository = appContainer.clientRepository,
                        visitRepository = appContainer.visitRepository,
                        workSessionRepository = appContainer.visitWorkSessionRepository,
                        inspectionRepository = appContainer.inspectionRepository,
                        quoteRepository = appContainer.quoteRepository,
                        materialRepository = appContainer.materialRepository,
                        technicalCalculationRepository = appContainer.technicalCalculationRepository,
                        financeRepository = appContainer.financeRepository,
                        reminderRepository = appContainer.reminderRepository,
                        settingsRepository = appContainer.reminderSettingsRepository,
                        reminderCoordinator = appContainer.reminderCoordinator,
                        initialSharedText = sharedText,
                        initialVisitId = initialVisitId,
                    )
                }
            }
        }
    }
}
