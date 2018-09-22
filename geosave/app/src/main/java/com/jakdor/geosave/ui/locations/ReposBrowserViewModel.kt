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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.jakdor.geosave.arch.BaseViewModel
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.common.model.firebase.User
import com.jakdor.geosave.common.wrapper.FirebaseAuthWrapper
import com.jakdor.geosave.utils.RxSchedulersFacade
import timber.log.Timber
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * ViewModel for [ReposBrowserFragment]
 */
class ReposBrowserViewModel @Inject
constructor(application: Application, rxSchedulersFacade: RxSchedulersFacade,
            private val firebaseAuthWrapper: FirebaseAuthWrapper,
            private val db: FirebaseFirestore):
    BaseViewModel(application, rxSchedulersFacade){

    val dialogLunchRequest = MutableLiveData<Int>()
    val dialogDismissRequest = MutableLiveData<Int>()
    val dialogLoadingStatus = MutableLiveData<Boolean>()
    val reposList = MutableLiveData<MutableList<Repo?>>()

    private var reposRequest = false

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
        if(dialogLoadingStatus.value == false) dialogDismissRequest.postValue(0)
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
        if(!reposRequest){
            reposRequest = true
            loadingStatus.postValue(true)

            val reposResponse = mutableListOf<Repo?>()

            if(firebaseAuthWrapper.userObjectSnapshot != null){
                val userObj = firebaseAuthWrapper.userObjectSnapshot?.toObject(User::class.java)
                userObj?.reposList?.forEach { documentReference: DocumentReference ->
                    documentReference.get().addOnSuccessListener { repo ->
                        reposResponse.add(repo.toObject(Repo::class.java))
                        if(reposResponse.size == userObj.reposList.size){
                            reposRequest = false
                            handleNewRepoList(reposResponse)
                        }
                    }
                }
            } else {
                db.collection("users").document(firebaseAuthWrapper.getUid()!!).get()
                        .addOnSuccessListener {
                            val reposRefList = it.toObject(User::class.java)?.reposList
                            if (reposRefList != null) {
                                for (repoRef in reposRefList) {
                                    repoRef.get().addOnSuccessListener { repo ->
                                        reposResponse.add(repo.toObject(Repo::class.java))
                                        if (reposResponse.size == reposRefList.size){
                                            reposRequest = false
                                            handleNewRepoList(reposResponse)
                                        }
                                    }
                                }
                            } else {
                                reposRequest = false
                                Timber.e("Unable to fetch user repos, user object is null")
                            }
                        }.addOnFailureListener {
                            reposRequest = false
                            Timber.e("Unable to fetch user repos: %s", it.toString())
                        }
            }
        }
    }

    /**
     * Forward new reposList to view layer
     */
    private fun handleNewRepoList(repos: MutableList<Repo?>){
        val sortedRepos = repos.sortedBy { repo -> repo?.name } as MutableList<Repo?>
        loadingStatus.postValue(false)
        reposList.postValue(sortedRepos)
        Timber.i("Fetched user repos from firestore")
    }

    /**
     * Push new repo to firestore
     */
    fun createNewRepo(repo: Repo){
        dialogLoadingStatus.postValue(true)

        if(firebaseAuthWrapper.getUid() != null) {
            repo.ownerUid = firebaseAuthWrapper.getUid()!!
        } else {
            Timber.wtf("User not logged in")
            return
        }

        db.runTransaction { transaction ->
            val userObjRef = db.collection("users").document(firebaseAuthWrapper.getUid()!!)
            val repoRef = db.collection("repos").document()
            val userObjSnap = transaction.get(userObjRef)
            firebaseAuthWrapper.userObjectSnapshot = userObjSnap

            val userObj = userObjSnap.toObject(User::class.java)
            userObj?.reposList?.add(userObj.reposList.size, repoRef)

            transaction.set(repoRef, repo)
            if(userObj != null){
                transaction.set(userObjRef, userObj)
            } else {
                throw FirebaseFirestoreException("User obj null",
                        FirebaseFirestoreException.Code.ABORTED)
            }
        }.addOnCompleteListener {
            dialogLoadingStatus.postValue(false)
        }.addOnSuccessListener {
            dialogDismissRequest.postValue(0)
            Timber.i("Created new repository")
        }.addOnFailureListener {
            Timber.e("Unable to create new repository: %s", it.toString())
        }
    }
}