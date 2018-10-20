/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.locations

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.jakdor.geosave.arch.BaseViewModel
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.common.model.firebase.User
import com.jakdor.geosave.common.repository.CameraRepository
import com.jakdor.geosave.common.repository.PictureStorageRepository
import com.jakdor.geosave.common.repository.ReposRepository
import com.jakdor.geosave.common.repository.UserRepository
import com.jakdor.geosave.common.wrapper.FirebaseAuthWrapper
import com.jakdor.geosave.utils.RxSchedulersFacade
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class RepoViewModel @Inject
constructor(application: Application, rxSchedulersFacade: RxSchedulersFacade,
            private val reposRepository: ReposRepository,
            private val userRepository: UserRepository,
            private val firebaseAuthWrapper: FirebaseAuthWrapper,
            private val cameraRepository: CameraRepository,
            private val pictureStorageRepository: PictureStorageRepository):
        BaseViewModel(application, rxSchedulersFacade) {

    val repoIsOwnerPair = MutableLiveData<Pair<Repo?, Boolean>>()
    val repoContributorPicUrl = MutableLiveData<String>()
    val dialogLunchRequest = MutableLiveData<RepoFragment.DialogRequest>()
    val dialogDismissRequest = MutableLiveData<RepoFragment.DialogRequest>()
    val dialogLoadingStatus = MutableLiveData<Boolean>()
    val dialogAddImagePicFile = MutableLiveData<File>()

    private lateinit var addImagePictureHandle: File
    private var subscribedToPictureUploadStatus = false
    private var repoIndex = -1

    /**
     * Observe [ReposRepository] chosen repository index stream
     */
    fun observeChosenRepository(){
        disposable.add(reposRepository.chosenRepositoryIndexStream
                .subscribeOn(rxSchedulersFacade.io())
                .observeOn(rxSchedulersFacade.ui())
                .subscribe(
                        { result -> if(result != -1){
                            repoIndex = result
                            val isOwner = checkIsOwner(reposRepository.reposListStream.value[result])
                            val resultPair = Pair(reposRepository.reposListStream.value[result], isOwner)
                            repoIsOwnerPair.postValue(resultPair)
                        }},
                        {e -> Timber.e("Chosen repository index returned: %s", e.toString())}
                ))

        disposable.add(reposRepository.notifyChosenRepoUpdate
                .subscribeOn(rxSchedulersFacade.io())
                .observeOn(rxSchedulersFacade.ui())
                .subscribe(
                        { result -> if(result) updateCurrentRepoView()},
                        { e -> Timber.e("Error chosen repo update: %s", e.toString())}
                ))
    }

    /**
     * Check if user is owner of repository, call after receiving current repoIsOwnerPair from [ReposRepository]
     */
    fun checkIsOwner(repo: Repo?): Boolean{
        return firebaseAuthWrapper.getUid() == repo?.ownerUid
    }

    /**
     * Force update current repo view in fragment
     */
    private fun updateCurrentRepoView(){
        val repo = reposRepository.reposListStream.value[repoIndex]
        val isOwner = checkIsOwner(repo)
        repoIsOwnerPair.postValue(Pair(repo, isOwner))
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
        if(repoIsOwnerPair.value != null && repoIsOwnerPair.value!!.second)
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
     * Handle AddImageDialog get photo option,
     * request camera photos from [CameraRepository]
     */
    fun onGetPhotoClicked(cameraFeature: CameraRepository.CameraFeature){
        cameraRepository.requestCameraPhotos(CLASS_TAG, cameraFeature)
    }

    /**
     * Observe [CameraRepository] for cameraResult
     */
    fun observeCameraResult(){
        disposable.add(cameraRepository.cameraResult
                .subscribeOn(rxSchedulersFacade.io())
                .observeOn(rxSchedulersFacade.io())
                .subscribe(
                        { result -> handleCameraResult(result)},
                        { e -> Timber.e("error observing cameraResult: %s", e.toString()) }
                ))
    }

    /**
     * Handle new cameraRequestResult
     */
    fun handleCameraResult(cameraRequestResult: CameraRepository.CameraRequestResult){
        if(cameraRequestResult.tag == CLASS_TAG) {
            Timber.i("viewModel got photo handle")
            addImagePictureHandle = cameraRequestResult.file
            dialogAddImagePicFile.postValue(addImagePictureHandle)
        }
    }

    /**
     * Compress picture before upload
     */
    fun uploadPicCompress(){
        if(::addImagePictureHandle.isInitialized) {
            disposable.add(pictureStorageRepository.compressPicture(addImagePictureHandle)
                    .subscribe(
                            { result -> Timber.i("Successfully compressed pic")
                                uploadPic(result) },
                            { e -> Timber.e("Unable to compress pic: %s", e.toString())
                                uploadPic(null)
                            }))
        }
    }

    /**
     * Initiate picture upload to firestore
     */
    fun uploadPic(pic: Bitmap?){
        if(!subscribedToPictureUploadStatus){
            disposable.add(pictureStorageRepository.pictureUploadStatus
                    .subscribeOn(rxSchedulersFacade.io())
                    .observeOn(rxSchedulersFacade.io())
                    .subscribe(
                            { result -> when(result){
                                PictureStorageRepository.RequestStatus.IDLE -> {}
                                PictureStorageRepository.RequestStatus.READY -> {
                                    dialogLoadingStatus.postValue(false)
                                    dialogDismissRequest.postValue(
                                            RepoFragment.DialogRequest.ADD_IMAGE)
                                }
                                PictureStorageRepository.RequestStatus.ONGOING -> {
                                    dialogLoadingStatus.postValue(true)
                                }
                                PictureStorageRepository.RequestStatus.ERROR -> {
                                    dialogLoadingStatus.postValue(false)
                                    Timber.e("Picture upload error")
                                }
                                PictureStorageRepository.RequestStatus.NO_NETWORK -> {
                                    dialogLoadingStatus.postValue(false)
                                    dialogDismissRequest.postValue(
                                            RepoFragment.DialogRequest.ADD_IMAGE_NO_NET)
                                    Timber.e("No network error")
                                }
                                else -> {
                                    dialogLoadingStatus.postValue(false)
                                    Timber.e("Error observing pictureUploadStatus")
                                }
                            }},
                            { e -> Timber.e("Error observing pictureUploadStatus, %s",
                                    e.toString()) }
                    ))
        }

        if(repoIsOwnerPair.value?.first != null && repoIndex != -1) {
            if(pic != null) {
                pictureStorageRepository.uploadRepositoryPicture(
                        repoIsOwnerPair.value!!.first!!, repoIndex, pic)
            } else {
                pictureStorageRepository.uploadRepositoryPicture(
                        repoIsOwnerPair.value!!.first!!, repoIndex, addImagePictureHandle)
            }
        }
    }

    companion object {
        private const val CLASS_TAG: String = "RepoViewModel"
    }
}