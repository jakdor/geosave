package com.jakdor.geosave.main

/**
 * Defines MainActivity behaviour
 */
interface MainContract {

    interface MainView{
        fun switchToGpsInfoFragment()
    }

    interface MainPresenter
}