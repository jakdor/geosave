package com.jakdor.geosave.ui.main

/**
 * Defines MainActivity behaviour
 */
interface MainContract {

    interface MainView {
        fun switchToGpsInfoFragment()
        fun switchToMapFragment()
        fun switchToLocationsFragment()
    }

    interface MainPresenter {
        fun onGpsInfoTabClicked()
        fun onMapTabClicked()
        fun onLocationsTabClicked()
    }
}