package com.jakdor.geosave.ui.main

import com.jakdor.geosave.service.gps.GpsListenerService

/**
 * Defines MainActivity behaviour
 */
interface MainContract {

    interface MainView {
        fun switchToGpsInfoFragment()
        fun switchToMapFragment()
        fun switchToLocationsFragment()
        fun getLocationPermission()
    }

    interface MainPresenter {
        fun onGpsInfoTabClicked()
        fun onMapTabClicked()
        fun onLocationsTabClicked()
        fun attachService(gpsListenerService: GpsListenerService)
        fun detachService()
        fun isServiceAttached(): Boolean
        fun gpsPermissionStatus(granted: Boolean)
    }
}