package com.example.parabdcollector.utils

import android.content.Context
import com.google.ar.core.ArCoreApk

/**
 * Helper to manage ARCore availability and session checks.
 */
object ARCoreHelper {

    /**
     * Checks if ARCore is supported on this device.
     */
    fun isARCoreSupported(context: Context): Boolean {
        val availability = ArCoreApk.getInstance().checkAvailability(context)
        return availability.isSupported
    }

    /**
     * Checks if ARCore is installed or needs update.
     */
    fun isARCoreReady(context: Context): Boolean {
        val availability = ArCoreApk.getInstance().checkAvailability(context)
        return availability == ArCoreApk.Availability.SUPPORTED_INSTALLED
    }
}
