package com.jakdor.geosave.ui.main

import com.jakdor.geosave.mvp.BasePresenter
import com.jakdor.geosave.service.gps.GpsListenerService
import timber.log.Timber

class MainPresenter(view: MainContract.MainView):
        BasePresenter<MainContract.MainView>(view),
        MainContract.MainPresenter{

    private var currentTab = -1

    private var gpsListenerService: GpsListenerService? = null

    override fun start() {
        super.start()
        view?.switchToGpsInfoFragment()
        currentTab = 0
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
     * Attach [GpsListenerService] to presenter
     */
    override fun attachService(gpsListenerService: GpsListenerService) {
        this.gpsListenerService = gpsListenerService
        Timber.i("attached GpsListenerService")
        gpsListenerService.test()
    }

    /**
     * Detach [GpsListenerService] form presenter
     */
    override fun detachService() {
        gpsListenerService = null
        Timber.i("detached GpsListenerService")
    }
}