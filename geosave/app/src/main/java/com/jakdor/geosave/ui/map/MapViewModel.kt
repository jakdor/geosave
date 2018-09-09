package com.jakdor.geosave.ui.map

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.jakdor.geosave.arch.BaseViewModel
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.common.repository.SharedPreferencesRepository
import com.jakdor.geosave.common.repository.UserLocationObserver
import com.jakdor.geosave.utils.RxSchedulersFacade
import timber.log.Timber
import javax.inject.Inject

class MapViewModel @Inject
constructor(application: Application,
            rxSchedulersFacade: RxSchedulersFacade,
            private val gpsInfoRepository: GpsInfoRepository,
            private val sharedPreferencesRepository: SharedPreferencesRepository):
        BaseViewModel(application, rxSchedulersFacade){

    val location = MutableLiveData<UserLocation>()
    val mapType = MutableLiveData<Int>()
    val locationType = MutableLiveData<Int>()

    /**
     * Handle user changed map type
     */
    fun onMapTypeClicked(id: Int){
        if(mapType.value != id) {
            mapType.postValue(id)
            sharedPreferencesRepository.save(SharedPreferencesRepository.mapTypeKey, id)
            Timber.i("Map type changed, %d", id)
        }
    }

    /**
     * Load saved preferences
     */
    fun loadPreferences(){
        val mapTypeVal = sharedPreferencesRepository.getInt(
                SharedPreferencesRepository.mapTypeKey, 0)
        val locationTypeVal = sharedPreferencesRepository.getString(
                SharedPreferencesRepository.locationUnits, "0").toInt()
        mapType.postValue(mapTypeVal)
        locationType.postValue(locationTypeVal)
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