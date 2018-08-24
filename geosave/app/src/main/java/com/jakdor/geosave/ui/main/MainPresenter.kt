package com.jakdor.geosave.ui.main

import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.arch.BasePresenter

class MainPresenter(view: MainContract.MainView, private val gpsInfoRepository: GpsInfoRepository):
        BasePresenter<MainContract.MainView>(view),
        MainContract.MainPresenter{

    private var currentTab = -1

    private var locationUpdates = false
    private var fallBackMode = false
    private var fallbackLocationUpdates = false

    override fun start() {
        super.start()
        if(currentTab == -1) { //first load
            view?.switchToGpsInfoFragment()
            currentTab = 0
        } else { //presenter reattached after screen rotation
            when (currentTab) {
                0 -> view?.switchToGpsInfoFragment()
                1 -> view?.switchToMapFragment()
                2 -> view?.switchToLocationsFragment()
            }
        }
    }

    override fun pause() {
        super.pause()
        if(locationUpdates){
            view?.stopLocationUpdates()
        } else if (fallbackLocationUpdates){
            view?.fallbackStopLocationUpdates()
        }
    }

    override fun resume() {
        super.resume()
        if(locationUpdates){
            view?.gmsSetupLocationUpdates()
        } else if (fallbackLocationUpdates){
            fallbackStartup()
        }
    }

    /**
     * Gps info navigation icon clicked
     */
    override fun onGpsInfoTabClicked() {
        if(currentTab != 0){
            view?.switchToGpsInfoFragment()
            currentTab = 0
        }
    }

    /**
     * Map navigation icon clicked
     */
    override fun onMapTabClicked() {
        if(currentTab != 1){
            view?.switchToMapFragment()
            currentTab = 1
        }
    }

    /**
     * Locations navigation icon clicked
     */
    override fun onLocationsTabClicked() {
        if(currentTab != 2){
            view?.switchToLocationsFragment()
            currentTab = 2
        }
    }

    /**
     * Add location menu option clicked
     */
    override fun onAddOptionClicked() {

    }

    /**
     * Called on GMS connection established
     */
    override fun gmsConnected() {
        view?.checkPermissions()
    }

    /**
     * Called on GMS connection suspended
     */
    override fun gmsSuspended() {

    }

    /**
     * Called on GMS connection failed
     */
    override fun gmsFailed() {
        fallBackMode = true
        view?.checkPermissions()
    }

    /**
     * Called on GMS locationChangedListener() / native LocationManager onLocationChanged() called
     */
    override fun onLocationChanged(userLocation: UserLocation) {
        gpsInfoRepository.next(userLocation)
    }

    /**
     * Called on GMS location callback add
     */
    override fun gmsLocationUpdatesActive() {
        locationUpdates = true
    }

    /**
     * Handle permissions status
     */
    override fun permissionsGranted(status: Boolean) {
        when(status){
            true -> {
                if(!fallBackMode){
                    view?.gmsSetupLocationUpdates()
                } else {
                    fallbackStartup()
                }
            }
            false -> view?.displayToast(R.string.gps_permissions_declined)
        }
    }

    /**
     * Handle gps enable dialog
     */
    override fun gmsGpsEnableDialog(result: Boolean) {
        when(result){
            true -> view?.gmsSetupLocationUpdates()
            false -> view?.displayToast(R.string.gps_enable_declined)
        }
    }

    /**
     * Setup and configure fallback location listener
     */
    override fun fallbackStartup() {
        view?.fallbackLocationManagerSetup()
        view?.fallbackCheckGps()
        view?.fallbackStartLocationUpdates()
    }

    /**
     * Fallback for GMS GPS auto-enable fail
     */
    override fun fallbackGpsAutoEnableFailed() {
       view?.fallbackLocationManagerSetup()
       view?.fallbackCheckGps()
    }

    /**
     * Handle user action in fallback gps turn on dialog
     */
    override fun fallbackGpsDialogUserResponse(response: Boolean) {
        when(response){
            true -> view?.fallbackTurnGpsIntent()
            false -> view?.displayToast(R.string.gps_fallback_dialog_no_toast)
        }
    }

    /**
     * Called on fallback gps location callback add
     */
    override fun fallbackLocationUpdatesActive() {
        fallbackLocationUpdates = true
    }

    /**
     * Firebase handle result of sign-in
     */
    override fun firebaseSignIn(status: Boolean) {
        if(status) {
            view?.firebaseSendEmailVerification()
        }
    }

    /**
     * Firebase login loggedIn changed
     */
    override fun firebaseLogin(loggedIn: Boolean) {
        if(!loggedIn){
            view?.displayFirstStartupDialog()
        }
    }

    override fun onFirstStartupDialogResult(response: Boolean) {
        if(response){
            view?.firebaseSignInIntent()
        } else {
            view?.firebaseLoginAnonymous()
        }
    }
}