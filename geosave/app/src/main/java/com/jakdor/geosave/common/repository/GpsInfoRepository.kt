package com.jakdor.geosave.common.repository

import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.utils.RxSchedulersFacade
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Singleton

/**
 * Repository for [UserLocation] objects managed by reactive stream
 */
@Singleton
class GpsInfoRepository(private val schedulers: RxSchedulersFacade){

    private val stream: BehaviorSubject<UserLocation> = BehaviorSubject.create()

    /**
     * post new [UserLocation] to [BehaviorSubject] stream
     */
    fun next(userLocation: UserLocation){
        stream.onNext(userLocation)
    }

    /**
     * Subscribe to [BehaviorSubject] stream
     */
    fun subscribe(observer: UserLocationObserver): Disposable{
        return stream.subscribeOn(schedulers.computation()).subscribeWith(observer)
    }
}