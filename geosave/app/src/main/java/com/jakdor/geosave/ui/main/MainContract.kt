/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.main

import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.common.repository.CameraRepository
import java.io.File

/**
 * Defines MainActivity behaviour
 */
interface MainContract {

    interface MainView {
        fun switchToGpsInfoFragment()
        fun switchToMapFragment()
        fun switchToLocationsFragment()
        fun switchToPreferencesFragment()

        fun displayToast(strId: Int)

        fun lunchFirstStartupDialog()
        fun lunchAddLocationDialog()
        fun loadAddLocationDialogRepoSpinner(
                indexRepoNamePair: ArrayList<Pair<Int, String>>, selectedIndex: Int)
        fun setAddLocationDialogLoadingStatus(status: Boolean)
        fun dismissAddLocationDialog()
        fun shareIntent(text: String)

        fun checkPermissions()

        fun gmsSetupLocationUpdates()
        fun stopLocationUpdates()

        fun fallbackCheckGps()
        fun fallbackTurnGpsIntent()
        fun fallbackLocationManagerSetup()
        fun fallbackStartLocationUpdates()
        fun fallbackStopLocationUpdates()

        fun firebaseSignInIntent()

        fun cameraRequest(cameraFeature: CameraRepository.CameraFeature)
        fun checkCameraPermissions()
    }

    interface MainPresenter {
        fun onGpsInfoTabClicked()
        fun onMapTabClicked()
        fun onLocationsTabClicked()

        fun onAddOptionClicked()
        fun onAddLocationDialogUploadClicked(repoIndex: Int, name: String, info: String)

        fun onPreferencesOptionClicked()
        fun switchBackFromPreferenceFragment(): Boolean

        fun onShareOptionClicked()

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

        fun notifyPossiblePreferencesChange()

        fun cameraPermissionsGranted(status: Boolean)
        fun onCameraResult(file: File)
    }
}