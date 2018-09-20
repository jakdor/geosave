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
import com.google.firebase.firestore.FirebaseFirestore
import com.jakdor.geosave.arch.BaseViewModel
import com.jakdor.geosave.utils.RxSchedulersFacade
import javax.inject.Inject

/**
 * ViewModel for [ReposBrowserFragment]
 */
class ReposBrowserViewModel @Inject
constructor(application: Application, rxSchedulersFacade: RxSchedulersFacade,
            private val db: FirebaseFirestore):
    BaseViewModel(application, rxSchedulersFacade){

    val dialogLunchRequest = MutableLiveData<Int>()
    val dialogDismissRequest = MutableLiveData<Int>()
    val dialogLoadingStatus = MutableLiveData<Boolean>()

    init {
        loadingStatus.postValue(false)
        dialogLoadingStatus.postValue(false)
    }

    /**
     * Handle click on create new repo fab
     */
    fun onFabCreateNewClicked(){
        dialogDismissRequest.postValue(-1)
        dialogLunchRequest.postValue(0)
    }

    /**
     * Handle click on browse public repos fab
     */
    fun onFabBrowsePublicClicked(){
        dialogDismissRequest.postValue(-1)
        dialogLunchRequest.postValue(1)
    }

    /**
     * Handle click on join private repo fab
     */
    fun onFabJoinPrivateClicked(){
        dialogDismissRequest.postValue(-1)
        dialogLunchRequest.postValue(2)
    }

    /**
     * Handle click on cancel in [com.jakdor.geosave.ui.elements.AddRepoDialog]
     */
    fun onAddRepoDialogCancelClicked(){
        if(dialogLoadingStatus.value != true) dialogDismissRequest.postValue(0)
    }

    /**
     * Handle click on crate in [com.jakdor.geosave.ui.elements.AddRepoDialog]
     */
    fun onAddRepoDialogCreateClicked(){
        dialogLoadingStatus.postValue(true)
        //todo create query
    }
}