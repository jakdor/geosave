package com.jakdor.geosave.ui.main

import com.jakdor.geosave.common.model.UserLocation

/**
 * Defines MainActivity behaviour
 */
interface MainContract {

    interface MainView {
        fun switchToGpsInfoFragment()
        fun switchToMapFragment()
        fun switchToLocationsFragment()

        fun displayToast(strId: Int)

        fun displayFirstStartupDialog()

        fun checkPermissions()

        fun gmsSetupLocationUpdates()
        fun stopLocationUpdates()

        fun fallbackCheckGps()
        fun fallbackTurnGpsIntent()
        fun fallbackLocationManagerSetup()
        fun fallbackStartLocationUpdates()
        fun fallbackStopLocationUpdates()

        fun firebaseSignInIntent()
    }

    interface MainPresenter {
        fun onGpsInfoTabClicked()
        fun onMapTabClicked()
        fun onLocationsTabClicked()

        fun onAddOptionClicked()

        fun onLocationChanged(userLocation: UserLocation)
        fun gmsConnected()
        fun gmsSuspended()
        fun gmsFailed()
        fun gmsLocationUpdatesActive()

        fun permissionsGranted(status: Boolean)
        fun gmsGpsEnableDialog(result: Boolean)

        fun fallbackStartup()
        fun fallbackGpsAutoEnableFailed()
        fun fallbackGpsDialogUserResponse(response: Boolean)
        fun fallbackLocationUpdatesActive()

        fun firebaseSignIn(status: Boolean)
        fun firebaseLogin(loggedIn: Boolean)

        fun onFirstStartupDialogResult(response: Boolean)
    }
}