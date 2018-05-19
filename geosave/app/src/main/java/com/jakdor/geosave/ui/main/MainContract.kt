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
        fun stopLocationUpdates()

        fun displayToast(strId: Int)

        fun fallbackCheckGps()
        fun fallbackTurnGpsIntent()
    }

    interface MainPresenter {
        fun onGpsInfoTabClicked()
        fun onMapTabClicked()
        fun onLocationsTabClicked()

        fun gmsConnected()
        fun gmsSuspended()
        fun gmsFailed()
        fun gmsLocationChanged()
        fun gmsLocationUpdatesActive()

        fun permissionsGranted(status: Boolean)
        fun gmsGpsEnableDialog(result: Boolean)

        fun fallbackGpsAutoEnableFailed()
        fun fallbackGpsDialogUserResponse(response: Boolean)
    }
}