package com.jakdor.geosave.ui.main

/**
 * Defines MainActivity behaviour
 */
interface MainContract {

    interface MainView {
        fun switchToGpsInfoFragment()
        fun switchToMapFragment()
        fun switchToLocationsFragment()

        fun checkPermissions()

        fun gmsSetupLocationUpdates()

        fun displayToast(strId: Int)
    }

    interface MainPresenter {
        fun onGpsInfoTabClicked()
        fun onMapTabClicked()
        fun onLocationsTabClicked()

        fun gmsConnected()
        fun gmsSuspended()
        fun gmsFailed()
        fun gmsLocationChanged()

        fun permissionsGranted(status: Boolean)
        fun gpsEnableDialog(result: Boolean)
    }
}