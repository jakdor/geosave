package com.jakdor.geosave.ui.main

import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.arch.BasePresenter
import com.jakdor.geosave.common.wrapper.FirebaseAuthWrapper

class MainPresenter(view: MainContract.MainView,
                    private val gpsInfoRepository: GpsInfoRepository,
                    private val firebaseAuthWrapper: FirebaseAuthWrapper):
        BasePresenter<MainContract.MainView>(view),
        MainContract.MainPresenter{

    private var currentTab = -1
    private var backTab = -1

    private var locationUpdates = false
    private var fallBackMode = false
    private var fallbackLocationUpdates = false

    /**
     * Check firebase login
     */
    override fun start() {
        super.start()
        firebaseLogin(firebaseAuthWrapper.isLoggedIn())
    }

    /**
     * Load fragment on create view state
     */
    override fun create() {
        super.start()
        if(currentTab == -1) { //first load
            view?.switchToGpsInfoFragment()
            currentTab = 0
        } else { //presenter reattached after screen rotation
            when (currentTab) {
                0 -> view?.switchToGpsInfoFragment()
                1 -> view?.switchToMapFragment()
                2 -> view?.switchToLocationsFragment()
                3 -> view?.switchToPreferencesFragment()
            }
        }
    }

    /**
     * Remove location updates on pause view state
     */
    override fun pause() {
        super.pause()
        if(locationUpdates){
            view?.stopLocationUpdates()
            gpsInfoRepository.stopElevationApiCalls()
            gpsInfoRepository.clearDisposable()
        } else if (fallbackLocationUpdates){
            view?.fallbackStopLocationUpdates()
        }
    }

    /**
     * Resume location updates on resume view state
     */
    override fun resume() {
        super.resume()
        if(locationUpdates){
            view?.gmsSetupLocationUpdates()
            gpsInfoRepository.startElevationApiCalls()
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
        backTab = -1
    }

    /**
     * Map navigation icon clicked
     */
    override fun onMapTabClicked() {
        if(currentTab != 1){
            view?.switchToMapFragment()
            currentTab = 1
        }
        backTab = -1
    }

    /**
     * Locations navigation icon clicked
     */
    override fun onLocationsTabClicked() {
        if(currentTab != 2){
            view?.switchToLocationsFragment()
            currentTab = 2
        }
        backTab = -1
    }

    /**
     * Add location menu option clicked
     */
    override fun onAddOptionClicked() {

    }

    /**
     * Preferences menu option clicked
     */
    override fun onPreferencesOptionClicked() {
        if(currentTab != 3) {
            backTab = currentTab
            currentTab = 3
            view?.switchToPreferencesFragment()
        }
    }

    /**
     * Get back to fragment id in backTab variable
     */
    override fun switchBackFromPreferenceFragment(): Boolean {
        return if(backTab != -1) {
            when (backTab) {
                0 -> onGpsInfoTabClicked()
                1 -> onMapTabClicked()
                2 -> onLocationsTabClicked()
            }
            true
        } else false
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
                    gpsInfoRepository.startElevationApiCalls()
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
            true -> {
                view?.gmsSetupLocationUpdates()
                gpsInfoRepository.startElevationApiCalls()
            }
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
            firebaseAuthWrapper.firebaseSendEmailVerification()
        } else {
            if(!firebaseAuthWrapper.isLoggedIn()) {
                firebaseAuthWrapper.firebaseLoginAnonymous()
            }
        }
    }

    /**
     * Firebase user login status changed
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
            firebaseAuthWrapper.firebaseLoginAnonymous()
        }
    }

    /**
     * Forward preferences changed event to [GpsInfoRepository]
     */
    override fun notifyPossiblePreferencesChange() {
        gpsInfoRepository.checkForPreferencesChange()
    }
}