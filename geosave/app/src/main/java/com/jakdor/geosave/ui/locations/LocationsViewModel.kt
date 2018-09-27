/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.locations

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.jakdor.geosave.arch.BaseViewModel
import com.jakdor.geosave.common.repository.ReposRepository
import com.jakdor.geosave.utils.RxSchedulersFacade
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for [LocationsFragment]
 */
class LocationsViewModel @Inject
constructor(application: Application, rxSchedulersFacade: RxSchedulersFacade,
            private val reposRepository: ReposRepository):
        BaseViewModel(application, rxSchedulersFacade){

    val currentFragmentId = MutableLiveData<String>()

    /**
     * Set initial child fragment, if user has chosen repository, switch to [RepoFragment],
     * if user discarded fragment or hasn't chosen yet, switch to [ReposBrowserFragment]
     */
    fun requestUpdatesOnCurrentFragment(){
        if(reposRepository.chosenRepositoryIndexStream.hasValue()){
            if(reposRepository.chosenRepositoryIndexStream.value != -1) {
                currentFragmentId.postValue(RepoFragment.CLASS_TAG)
            } else {
                currentFragmentId.postValue(ReposBrowserFragment.CLASS_TAG)
            }
        } else if(currentFragmentId.value.isNullOrEmpty()){
            currentFragmentId.postValue(ReposBrowserFragment.CLASS_TAG)
        }
    }

    /**
     * Observe for changes on [ReposRepository] chosenRepositoryIndexStream
     * switch to or discard [RepoFragment] according to received result
     */
    fun observeChosenRepositoryIndex(){
        disposable.add(reposRepository.chosenRepositoryIndexStream
                .subscribeOn(rxSchedulersFacade.io())
                .observeOn(rxSchedulersFacade.io())
                .subscribe (
                        { result ->
                            if(result != -1){
                                currentFragmentId.postValue(RepoFragment.CLASS_TAG)
                            } else {
                                currentFragmentId.postValue(ReposBrowserFragment.CLASS_TAG)
                            }},
                        { e -> Timber.e("unable to receive chosen repo: %s", e.toString())}
                )
        )
    }
}