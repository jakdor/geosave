package com.jakdor.geosave.service.gps

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import timber.log.Timber

/**
 * Service providing GPS location and info updates
 */
class GpsListenerService: Service() {

    fun test(){
        Timber.wtf("success")
    }

    private val localBinder: LocalBinder = LocalBinder()

    override fun onBind(p0: Intent?): IBinder {
        return localBinder
    }

    inner class LocalBinder : Binder() {
        internal val service: GpsListenerService
            get() = this@GpsListenerService
    }
}