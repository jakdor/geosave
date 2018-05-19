package com.jakdor.geosave.ui.main

import com.jakdor.geosave.R
import com.jakdor.geosave.mvp.BasePresenter

class MainPresenter(view: MainContract.MainView):
        BasePresenter<MainContract.MainView>(view),
        MainContract.MainPresenter{

    private var currentTab = -1

    private var locationUpdates = false
    private var fallBackMode = false
    private var fallbackLocationUpdates = false

    override fun start() {
        super.start()
        view?.switchToGpsInfoFragment()
        currentTab = 0
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
            view?.fallbackStartLocationUpdates()
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
     * Called on GMS locationChangedListener called
     */
    override fun locationChanged() {

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
                    view?.fallbackLocationManagerSetup()
                    view?.fallbackCheckGps()
                    view?.fallbackStartLocationUpdates()
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
     * Fallback for GMS GPS auto enable fail
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
}