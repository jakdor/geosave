package com.jakdor.geosave.ui.gpsinfo

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.jakdor.geosave.arch.BaseViewModel
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.repository.GpsInfoRepository
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
            private val gpsInfoRepository: GpsInfoRepository):
        BaseViewModel(application, rxSchedulersFacade){

    val userLocation = MutableLiveData<UserLocation>()

    /**
     * Observe [GpsInfoRepository] stream
     */
    fun requestUserLocationUpdates(){
        disposable.add(gpsInfoRepository.subscribe(DataObserver()))
        loadingStatus.value = true
    }

    /**
     * Forward new [UserLocation] object to [MutableLiveData]
     */
    private fun userLocationUpdate(data: UserLocation){
        userLocation.value = data
        loadingStatus.value = false
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