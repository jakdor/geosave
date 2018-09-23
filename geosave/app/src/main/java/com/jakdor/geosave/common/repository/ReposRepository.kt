/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.common.repository

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.common.model.firebase.User
import com.jakdor.geosave.common.wrapper.FirebaseAuthWrapper
import com.jakdor.geosave.utils.RxSchedulersFacade
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

/**
 * Handles loading and saving [Repo] objects from and to firestore
 */
class ReposRepository(private val schedulers: RxSchedulersFacade,
                      private val firebaseAuthWrapper: FirebaseAuthWrapper,
                      private val db: FirebaseFirestore) {

    val reposListStream: BehaviorSubject<MutableList<Repo?>> = BehaviorSubject.create()
    val reposLoadingStatusStream: BehaviorSubject<Boolean> = BehaviorSubject.create()
    val createNewRequestStatusStream: BehaviorSubject<RequestStatus> = BehaviorSubject.create()

    enum class RequestStatus {
        IDLE, READY, ONGOING, ERROR, NO_NETWORK
    }

    init {
        reposLoadingStatusStream.onNext(false)
        createNewRequestStatusStream.onNext(RequestStatus.IDLE)
    }

    private var reposRequest = false

    /**
     * Load user repos in background as soon as userObjectSnapshot becomes available
     */
    fun fastInitialReposLoad(){
        val disposable = CompositeDisposable()
        disposable.add(firebaseAuthWrapper.userObjectSnapshotFlag
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.io())
                .subscribe { result -> if(result){
                    loadUserRepos()
                    disposable.clear()
                }}
        )
    }

    /**
     * Get user repos from firestore
     */
    fun loadUserRepos(){
        if(!reposRequest){
            reposRequest = true
            reposLoadingStatusStream.onNext(true)

            val reposResponse = mutableListOf<Repo?>()

            if(firebaseAuthWrapper.userObjectSnapshot != null){
                val userObj = firebaseAuthWrapper.userObjectSnapshot?.toObject(User::class.java)
                if(userObj?.reposList != null && userObj.reposList.isEmpty()){
                    handleEmptyRepoList()
                }
                userObj?.reposList?.forEach { documentReference: DocumentReference ->
                    documentReference.get().addOnSuccessListener { repo ->
                        reposResponse.add(repo.toObject(Repo::class.java))
                        if(reposResponse.size == userObj.reposList.size){
                            handleNewRepoList(reposResponse)
                        }
                    }.addOnFailureListener{
                        reposRequest = false
                        reposLoadingStatusStream.onNext(false)
                        Timber.e("Unable to fetch user repos: %s", it.toString())
                    }
                }
            } else {
                val userUid: String
                if(firebaseAuthWrapper.getUid() != null) {
                     userUid = firebaseAuthWrapper.getUid()!!
                } else {
                    Timber.wtf("User not logged in")
                    return
                }

                db.collection("users").document(userUid).get()
                        .addOnSuccessListener {
                            val reposRefList = it.toObject(User::class.java)?.reposList
                            if (reposRefList != null) {
                                if(reposRefList.isEmpty()){
                                    handleEmptyRepoList()
                                }
                                reposRefList.forEach { documentReference: DocumentReference ->
                                    documentReference.get().addOnSuccessListener { repo ->
                                        reposResponse.add(repo.toObject(Repo::class.java))
                                        if (reposResponse.size == reposRefList.size){
                                            handleNewRepoList(reposResponse)
                                        }
                                    }
                                }
                            } else {
                                reposRequest = false
                                reposLoadingStatusStream.onNext(false)
                                Timber.e("Unable to fetch user repos, user object is null")
                            }
                        }.addOnFailureListener {
                            reposRequest = false
                            reposLoadingStatusStream.onNext(false)
                            Timber.e("Unable to fetch user repos: %s", it.toString())
                        }
            }
        }
    }

    /**
     * Forward new reposList to view layer
     */
    private fun handleNewRepoList(repos: MutableList<Repo?>){
        reposRequest = false
        val sortedRepos = repos.sortedBy { repo -> repo?.name } as MutableList<Repo?>
        reposLoadingStatusStream.onNext(false)
        reposListStream.onNext(sortedRepos)
        Timber.i("Fetched user repos from firestore")
    }

    /**
     * Handle user has no repos
     */
    private fun handleEmptyRepoList(){
        reposRequest = false
        reposLoadingStatusStream.onNext(false)
        reposListStream.onNext(mutableListOf())
        Timber.i("User has no repos")
    }

    /**
     * Push new repo to firestore
     */
    fun createNewRepo(repo: Repo){
        if(firebaseAuthWrapper.getUid() != null) {
            repo.ownerUid = firebaseAuthWrapper.getUid()!!
        } else {
            Timber.wtf("User not logged in")
            return
        }

        createNewRequestStatusStream.onNext(RequestStatus.ONGOING)

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
        }.addOnSuccessListener {
            addNewRepoToReposList(repo)
            createNewRequestStatusStream.onNext(RequestStatus.READY)
            createNewRequestStatusStream.onNext(RequestStatus.IDLE)
            Timber.i("Created new repository")
        }.addOnFailureListener {
            if(it.toString() == "com.google.firebase.firestore.FirebaseFirestoreException:" +
                    " UNAVAILABLE: Unable to resolve host firestore.googleapis.com"){
                createNewRequestStatusStream.onNext(RequestStatus.NO_NETWORK)
            } else {
                createNewRequestStatusStream.onNext(RequestStatus.ERROR)
            }
            createNewRequestStatusStream.onNext(RequestStatus.IDLE)
            Timber.e("Unable to create new repository: %s", it.toString())
        }
    }

    /**
     * Update recyclerView locally without waiting for update from firestore
     */
    private fun addNewRepoToReposList(repo: Repo){
        if(reposListStream.value != null){
            val newRepos = mutableListOf<Repo?>()
            newRepos.addAll(reposListStream.value)
            newRepos.add(repo)
            val sortedRepos = newRepos.sortedBy { it -> it?.name } as MutableList<Repo?>
            reposListStream.onNext(sortedRepos)
        } else {
            reposListStream.onNext(mutableListOf(repo))
        }
    }
}