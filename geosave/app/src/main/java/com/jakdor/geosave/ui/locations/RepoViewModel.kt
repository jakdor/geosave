/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.locations

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.jakdor.geosave.arch.BaseViewModel
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.common.model.firebase.User
import com.jakdor.geosave.common.repository.ReposRepository
import com.jakdor.geosave.common.repository.UserRepository
import com.jakdor.geosave.common.wrapper.FirebaseAuthWrapper
import com.jakdor.geosave.utils.RxSchedulersFacade
import timber.log.Timber
import javax.inject.Inject

class RepoViewModel @Inject
constructor(application: Application, rxSchedulersFacade: RxSchedulersFacade,
            private val reposRepository: ReposRepository,
            private val userRepository: UserRepository,
            private val firebaseAuthWrapper: FirebaseAuthWrapper):
        BaseViewModel(application, rxSchedulersFacade) {

    val repoIsOwnerPair = MutableLiveData<Pair<Repo?, Boolean>>()
    val repoContributorPicUrl = MutableLiveData<String>()
    val dialogLunchRequest = MutableLiveData<RepoFragment.DialogRequest>()
    val dialogDismissRequest = MutableLiveData<RepoFragment.DialogRequest>()
    val dialogLoadingStatus = MutableLiveData<Boolean>()

    /**
     * Observe [ReposRepository] chosen repository index stream
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
    fun checkIsOwner(repo: Repo?): Boolean{
        return firebaseAuthWrapper.getUid() == repo?.ownerUid
    }

    /**
     * Observe [ReposRepository] contributors pic url stream
     */
    fun observeContributorsPicUrls(){
        disposable.add(userRepository.userPicUrlStream
                .subscribeOn(rxSchedulersFacade.io())
                .observeOn(rxSchedulersFacade.io())
                .subscribe({ result -> repoContributorPicUrl.postValue(result)},
                        { e -> Timber.e("userPicUrlStream: %s", e.toString())})
        )
    }

    /**
     * Get contributors pictures urls
     */
    fun requestContributorsPicUrls(repo: Repo?){
        if(repo != null){
            var pics = 3
            if(checkIsOwner(repo)){
                val user = firebaseAuthWrapper.userObjectSnapshot?.toObject(User::class.java)
                if(user != null){
                    repoContributorPicUrl.postValue(user.picUrl)
                    --pics
                }
            } else {
                userRepository.getUserPicUrl(repo.ownerUid)
                --pics
            }

            for(i in 0 until pics){
                if(repo.editorsUidList.size > i){
                    userRepository.getUserPicUrl(repo.editorsUidList[i])
                }
            }
        }
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
        Timber.i("repo edit clicked")
    }

    /**
     * Handle on toolbar invite collaborators click
     */
    fun onInviteClick(){
        Timber.i("invite clicked")
    }

    /**
     * Handle on add repo image clicked
     */
    fun onAddImageClick(){
        dialogLunchRequest.postValue(RepoFragment.DialogRequest.ADD_IMAGE)
        Timber.i("add image clicked")
    }

    /**
     * Handle on location card click
     */
    fun onLocationCardClick(index: Int){
        Timber.i("Location no.%d clicked", index)
    }

    /**
     * Forward dialogRequest to view layer
     */
    fun dismissDialog(dialogRequest: RepoFragment.DialogRequest){
        dialogDismissRequest.postValue(dialogRequest)
        Timber.i("Dismissed %s dialog", dialogRequest.name)
    }

    /**
     * Initiate picture upload to firestore
     */
    fun uploadPic(){
        dialogLoadingStatus.postValue(true)
    }
}