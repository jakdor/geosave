package com.jakdor.geosave.ui.main

import com.jakdor.geosave.R
import com.jakdor.geosave.mvp.BasePresenter

class MainPresenter(view: MainContract.MainView):
        BasePresenter<MainContract.MainView>(view),
        MainContract.MainPresenter{

    private var currentTab = -1

    private var locationUpdates = false

    override fun start() {
        super.start()
        view?.switchToGpsInfoFragment()
        currentTab = 0
    }

    override fun pause() {
        super.pause()
        if(locationUpdates){
            view?.stopLocationUpdates()
        }
    }

    override fun resume() {
        super.resume()
        if(locationUpdates){
            view?.gmsSetupLocationUpdates()
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

    }

    /**
     * Called on GMS locationChangedListener called
     */
    override fun gmsLocationChanged() {

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
            true -> view?.gmsSetupLocationUpdates()
            false -> view?.displayToast(R.string.gps_permissions_declined)
        }
    }

    /**
     * Handle gps enable dialog
     */
    override fun gpsEnableDialog(result: Boolean) {
        when(result){
            true -> view?.gmsSetupLocationUpdates()
            false -> view?.displayToast(R.string.gps_enable_declined)
        }
    }
}