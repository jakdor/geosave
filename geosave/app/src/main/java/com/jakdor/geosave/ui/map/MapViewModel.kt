package com.jakdor.geosave.ui.map

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.jakdor.geosave.arch.BaseViewModel
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.common.repository.UserLocationObserver
import com.jakdor.geosave.utils.RxSchedulersFacade
import timber.log.Timber
import javax.inject.Inject

class MapViewModel @Inject
constructor(application: Application,
            rxSchedulersFacade: RxSchedulersFacade,
            private val gpsInfoRepository: GpsInfoRepository):
        BaseViewModel(application, rxSchedulersFacade){

    val location = MutableLiveData<UserLocation>()
    val mapType = MutableLiveData<Int>()

    /**
     * Handle user changed map type
     */
    fun onMapTypeClicked(id: Int){
        if(mapType.value != id) {
            mapType.postValue(id)
            Timber.i("Map type changed, %d", id)
        }
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