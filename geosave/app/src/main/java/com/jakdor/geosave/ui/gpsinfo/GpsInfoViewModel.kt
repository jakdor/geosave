/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.gpsinfo

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.jakdor.geosave.arch.BaseViewModel
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.common.repository.SharedPreferencesRepository
import com.jakdor.geosave.common.repository.UserLocationObserver
import com.jakdor.geosave.utils.RxSchedulersFacade
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for [GpsInfoFragment]
 */
class GpsInfoViewModel @Inject
constructor(application: Application,
            rxSchedulersFacade: RxSchedulersFacade,
            private val gpsInfoRepository: GpsInfoRepository,
            private val sharedPreferencesRepository: SharedPreferencesRepository):
        BaseViewModel(application, rxSchedulersFacade){

    val location = MutableLiveData<UserLocation>()
    val clipboardCopyQueue = MutableLiveData<String?>()
    val preferences = MutableLiveData<MutableMap<String, Int>>()

    /**
     * Get update on saved preferences from [SharedPreferencesRepository]
     */
    fun requestPreferencesUpdate(){
        sharedPreferencesRepository.loadInitialPrefs() //first app run, load initial values

        val preferencesMap: MutableMap<String, Int> = mutableMapOf(
                Pair(SharedPreferencesRepository.locationUnits, sharedPreferencesRepository
                        .getString(SharedPreferencesRepository.locationUnits, "0").toInt()),
                Pair(SharedPreferencesRepository.altUnits, sharedPreferencesRepository
                        .getString(SharedPreferencesRepository.altUnits, "0").toInt()),
                Pair(SharedPreferencesRepository.accUnits, sharedPreferencesRepository
                        .getString(SharedPreferencesRepository.accUnits, "0").toInt()),
                Pair(SharedPreferencesRepository.speedUnits, sharedPreferencesRepository
                        .getString(SharedPreferencesRepository.speedUnits, "0").toInt()))

        preferences.postValue(preferencesMap)
    }

    /**
     * Handle copy button click
     */
    fun onCopyButtonClicked(data: String){
        Timber.i("Copied to the clipboard: %s", data)
        clipboardCopyQueue.postValue(data)
    }

    /**
     * Observe [GpsInfoRepository] stream
     */
    fun requestUserLocationUpdates(){
        disposable.add(gpsInfoRepository.subscribe(DataObserver()))
        loadingStatus.postValue(true)
    }

    /**
     * Forward new [UserLocation] object to [MutableLiveData]
     */
    private fun userLocationUpdate(data: UserLocation){
        location.postValue(data)
        loadingStatus.postValue(false)
    }

    /**
     * [UserLocationObserver] implementation
     */
    inner class DataObserver: UserLocationObserver() {
        override fun onNext(t: UserLocation) {
            userLocationUpdate(t)
        }

        override fun onError(e: Throwable) {
            Timber.e(e)
        }

        override fun onComplete() {}
    }
}