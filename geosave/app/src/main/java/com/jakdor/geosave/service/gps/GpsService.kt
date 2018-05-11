package com.jakdor.geosave.service.gps

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import dagger.android.DaggerService
import javax.inject.Inject

/**
 * Service wrapper for [GpsListenerService] enables easy mocking in tests
 * and Service lifecycle decoupling
 */
class GpsService: DaggerService(){

    @Inject
    lateinit var gpsListenerService: GpsListenerService

    private val localBinder: LocalBinder = LocalBinder()

    override fun onBind(p0: Intent?): IBinder {
        return localBinder
    }

    inner class LocalBinder: Binder() {
        internal val service: GpsListenerService
            get() = this@GpsService.gpsListenerService
    }
}
