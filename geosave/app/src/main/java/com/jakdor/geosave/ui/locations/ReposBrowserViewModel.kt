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
import com.jakdor.geosave.App
import com.jakdor.geosave.R
import com.jakdor.geosave.arch.BaseViewModel
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.utils.RxSchedulersFacade
import timber.log.Timber
import javax.inject.Inject
import com.jakdor.geosave.common.repository.ReposRepository
import com.jakdor.geosave.ui.locations.ReposBrowserFragment.DialogRequest

/**
 * ViewModel for [ReposBrowserFragment]
 */
class ReposBrowserViewModel @Inject
constructor(application: Application, rxSchedulersFacade: RxSchedulersFacade,
            private val reposRepository: ReposRepository):
    BaseViewModel(application, rxSchedulersFacade){

    val dialogLunchRequest = MutableLiveData<DialogRequest>()
    val dialogDismissRequest = MutableLiveData<DialogRequest>()
    val dialogLoadingStatus = MutableLiveData<Boolean>()
    val toast = MutableLiveData<String>()
    val reposList = MutableLiveData<MutableList<Repo?>>()

    init {
        loadingStatus.postValue(false)
        dialogLoadingStatus.postValue(false)
    }

    /**
     * Handle click on create new repo fab
     */
    fun onFabCreateNewClicked(){
        dialogDismissRequest.postValue(DialogRequest.NONE)
        dialogLunchRequest.postValue(DialogRequest.CREATE_NEW)
    }

    /**
     * Handle click on browse public repos fab
     */
    fun onFabBrowsePublicClicked(){
        dialogDismissRequest.postValue(DialogRequest.NONE)
        dialogLunchRequest.postValue(DialogRequest.BROWSE_PUBLIC)
    }

    /**
     * Handle click on join private repo fab
     */
    fun onFabJoinPrivateClicked(){
        dialogDismissRequest.postValue(DialogRequest.NONE)
        dialogLunchRequest.postValue(DialogRequest.JOIN_PRIVATE)
    }

    /**
     * Handle click on cancel in [com.jakdor.geosave.ui.elements.AddRepoDialog]
     */
    fun onAddRepoDialogCancelClicked(){
        if(dialogLoadingStatus.value == false)
            dialogDismissRequest.postValue(DialogRequest.CREATE_NEW)
    }

    /**
     * Handle click on repository card in [com.jakdor.geosave.ui.adapters.RepositoryAdapter]
     */
    fun onRepositoryClicked(repo: Repo){
        Timber.i("Repository card clicked: %s", repo.name)
    }

    /**
     * Get user repos from firestore
     */
    fun loadUserRepos(){
        disposable.add(reposRepository.reposLoadingStatusStream
                .subscribeOn(rxSchedulersFacade.computation())
                .observeOn(rxSchedulersFacade.ui())
                .subscribe(
                        { result -> loadingStatus.postValue(result) },
                        { error -> loadingStatus.postValue(false)
                            Timber.e("loadingStatus: %s", error.toString()) }
                ))

        disposable.add(reposRepository.reposListStream
                .subscribeOn(rxSchedulersFacade.computation())
                .observeOn(rxSchedulersFacade.ui())
                .subscribe(
                        { result -> reposList.postValue(result) },
                        { error -> loadingStatus.postValue(false)
                            Timber.e("reposList: %s", error.toString()) }
                ))

        if (reposRepository.reposListStream.value != null) { //data available
            reposList.postValue(reposRepository.reposListStream.value)
        } else if(reposRepository.reposLoadingStatusStream.value == false){ //data not available
            reposRepository.loadUserRepos()
        }
    }

    /**
     * Push new repo to firestore
     */
    fun createNewRepo(repo: Repo){
        disposable.add(reposRepository.createNewRequestStatusStream
                .subscribeOn(rxSchedulersFacade.computation())
                .observeOn(rxSchedulersFacade.ui())
                .subscribe({ result -> when(result){
                            ReposRepository.RequestStatus.IDLE -> {}
                            ReposRepository.RequestStatus.READY -> {
                                dialogLoadingStatus.postValue(false)
                                dialogDismissRequest.postValue(DialogRequest.CREATE_NEW)
                            }
                            ReposRepository.RequestStatus.ONGOING -> {
                                dialogLoadingStatus.postValue(true)
                            }
                            ReposRepository.RequestStatus.ERROR -> {
                                dialogLoadingStatus.postValue(false)
                                toast.postValue(getApplication<App>()
                                        .getString(R.string.add_repo_error_toast))
                                Timber.e("Error while creating new repo")
                            }
                            ReposRepository.RequestStatus.NO_NETWORK -> {
                                dialogLoadingStatus.postValue(false)
                                toast.postValue(getApplication<App>()
                                        .getString(R.string.add_repo_no_network_toast))
                                Timber.e("No network while creating new repo")
                            }
                            null -> {
                                dialogLoadingStatus.postValue(false)
                                toast.postValue(getApplication<App>()
                                        .getString(R.string.add_repo_error_toast))
                                Timber.e("Error while creating new repo")
                            }
                        } },
                        { error -> dialogLoadingStatus.postValue(false)
                            toast.postValue(getApplication<App>()
                                    .getString(R.string.add_repo_error_toast))
                            Timber.e("loadingStatus: %s", error.toString()) }
                ))

        reposRepository.createNewRepo(repo)
    }
}