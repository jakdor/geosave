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
import com.jakdor.geosave.utils.RxSchedulersFacade
import javax.inject.Inject

/**
 * ViewModel for [LocationsFragment]
 */
class LocationsViewModel @Inject
constructor(application: Application, rxSchedulersFacade: RxSchedulersFacade):
        BaseViewModel(application, rxSchedulersFacade){

    val currentFragmentId = MutableLiveData<String>()

    /**
     * Set initial child fragment
     */
    fun requestUpdatesOnCurrentFragment(){
        if(currentFragmentId.value.isNullOrEmpty()){
            currentFragmentId.postValue(ReposBrowserFragment.CLASS_TAG)
        }
    }
}