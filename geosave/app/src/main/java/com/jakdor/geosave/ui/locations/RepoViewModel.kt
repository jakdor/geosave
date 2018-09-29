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
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.common.repository.ReposRepository
import com.jakdor.geosave.common.wrapper.FirebaseAuthWrapper
import com.jakdor.geosave.utils.RxSchedulersFacade
import timber.log.Timber
import javax.inject.Inject

class RepoViewModel @Inject
constructor(application: Application, rxSchedulersFacade: RxSchedulersFacade,
            private val reposRepository: ReposRepository,
            private val firebaseAuthWrapper: FirebaseAuthWrapper):
        BaseViewModel(application, rxSchedulersFacade) {

    val repoIsOwnerPair = MutableLiveData<Pair<Repo?, Boolean>>()

    /**
     * Observe [ReposRepository] chosen repository index
     */
    fun observeChosenRepository(){
        disposable.add(reposRepository.chosenRepositoryIndexStream
                .subscribeOn(rxSchedulersFacade.io())
                .observeOn(rxSchedulersFacade.ui())
                .subscribe(
                        {result -> if(result != -1){
                            val isOwner = checkIsOwner(reposRepository.reposListStream.value[result])
                            val resultPair = Pair(reposRepository.reposListStream.value[result], isOwner)
                            repoIsOwnerPair.postValue(resultPair)
                        }},
                        {e -> Timber.e("Chosen repository index returned: %s", e.toString())}
                ))
    }

    /**
     * Check if user is owner of repository, call after receiving current repoIsOwnerPair from [ReposRepository]
     */
    private fun checkIsOwner(repo: Repo?): Boolean{
        return firebaseAuthWrapper.getUid() == repo?.ownerUid
    }

    /**
     * Return from [ReposRepository] to [ReposBrowserFragment]
     */
    fun returnFromRepoFragment(){
        reposRepository.chosenRepositoryIndexStream.onNext(-1)
    }

    /**
     * Handle on toolbar edit button click
     */
    fun onEditRepoClick(){
        Timber.i("repoIsOwnerPair edit clicked")
    }
}