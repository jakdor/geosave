package com.jakdor.geosave.service.gps

import android.app.Service
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import timber.log.Timber

/**
 * Service providing GPS location and info updates
 */
class GpsListenerService: Service(), LocationListener {

    private var lastKnownLocation: Location? = null

    //location updates
    private var locationUpdates: Boolean = false
    lateinit var locationManager: LocationManager
    private var provider: String? = null

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)
    }

    /**
     * Config LocationManager
     */
    fun locationManagerSetup() {
        //get location updates
        provider = locationManager.getBestProvider(Criteria(), false)
    }

    /**
     * Check GPS enabled, handle situation if gps offline
     */
    fun checkGps(){
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //prompt user about gps turned off
        }
    }

    /**
     * Start receiving location updates
     */
    fun startLocationUpdates() {
        try {
            //location update if min 2m distance
            locationManager.requestLocationUpdates(provider, 1000, 2.0f, this)
            locationUpdates = true
        } catch (e: SecurityException){
            Timber.e("Unauthorised call for location updates request")
        }
    }

    override fun onLocationChanged(p0: Location?) {
        lastKnownLocation = p0
        useDeviceLocation(p0)
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }

    /**
     * Handle location update
     */
    fun useDeviceLocation(p0: Location?) {
        Timber.i("Location update: %s", p0.toString())
    }

    private val localBinder: LocalBinder = LocalBinder()

    override fun onBind(p0: Intent?): IBinder {
        checkGps()
        return localBinder
    }

    inner class LocalBinder : Binder() {
        internal val service: GpsListenerService
            get() = this@GpsListenerService
    }

    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}