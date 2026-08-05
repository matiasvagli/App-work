package com.matiasdev.elecapp.core.external

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

fun Context.tryStartActivity(intent: Intent): Boolean {
    return try {
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}
