package com.matiasdev.elecapp.features.electricaltools.ui

import android.content.Context
import android.content.Intent

fun shareCalculationText(context: Context, text: String, title: String = "Compartir cálculo") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, title))
}
