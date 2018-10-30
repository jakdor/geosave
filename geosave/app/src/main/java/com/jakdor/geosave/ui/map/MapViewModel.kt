/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.map

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.jakdor.geosave.arch.BaseViewModel
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.model.firebase.Location
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.common.repository.ReposRepository
import com.jakdor.geosave.common.repository.SharedPreferencesRepository
import com.jakdor.geosave.common.repository.UserLocationObserver
import com.jakdor.geosave.utils.RxSchedulersFacade
import io.reactivex.disposables.Disposable
import timber.log.Timber
import javax.inject.Inject

class MapViewModel @Inject
constructor(application: Application,
            rxSchedulersFacade: RxSchedulersFacade,
            private val gpsInfoRepository: GpsInfoRepository,
            private val sharedPreferencesRepository: SharedPreferencesRepository,
            private val reposRepository: ReposRepository):
        BaseViewModel(application, rxSchedulersFacade){

    val location = MutableLiveData<UserLocation>()
    val mapType = MutableLiveData<Int>()
    val locationType = MutableLiveData<Int>()
    val currentRepoLocationsList = MutableLiveData<MutableList<Location>>()
    val repoIndexPairList = MutableLiveData<ArrayList<Pair<Int, String>>>()

    private lateinit var repoDisposable: Disposable

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
     * Observe [ReposRepository] chosenRepositoryIndexStream
     */
    fun requestCurrentRepoLocationsUpdates(){
        disposable.add(reposRepository.chosenRepositoryIndexStream
                .observeOn(rxSchedulersFacade.io())
                .subscribeOn(rxSchedulersFacade.io())
                .subscribe(
                        { result -> if(result != -1) observeRepo(result) },
                        { e -> Timber.e("Error observing chosenRepositoryIndexStream: %s",
                                e.toString())}
                ))
    }

    /**
     * Observe [ReposRepository] reposListStream
     * @index current repository index
     */
    private fun observeRepo(index: Int){
        if(::repoDisposable.isInitialized){
            disposable.remove(repoDisposable)
            repoDisposable.dispose()
        }

        repoDisposable = reposRepository.reposListStream
                .observeOn(rxSchedulersFacade.io())
                .subscribeOn(rxSchedulersFacade.io())
                .subscribe(
                        { result -> if(result[index] != null){
                            currentRepoLocationsList.postValue(result[index]!!.locationsList)
                        }},
                        { e -> Timber.e("Error observing reposListStream: %s", e.toString())}
                )
    }

    /**
     * Request updates for repo spinner
     */
    fun requestRepoSpinnerUpdates() {
        disposable.add(reposRepository.reposListStream
                .subscribeOn(rxSchedulersFacade.io())
                .observeOn(rxSchedulersFacade.io())
                .subscribe(
                        { _ -> repoIndexPairList.postValue(getReposIndexPair()) },
                        { e -> Timber.e("Error observing reposListStream: %s", e.toString())}
                ))
    }

    /**
     * Return [ArrayList] of Index and [Repo] pairs
     */
    fun getReposIndexPair(): ArrayList<Pair<Int, String>> {
        val repoIndexPair = arrayListOf<Pair<Int, String>>()

        if(reposRepository.reposListStream.hasValue()){
            for(i in 0 until reposRepository.reposListStream.value.size){
                val currentRepo = reposRepository.reposListStream.value[i]
                if(currentRepo != null) {
                    repoIndexPair.add(Pair(i, currentRepo.name))
                }
            }
        }

        return repoIndexPair
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