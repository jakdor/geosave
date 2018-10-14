/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.common.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.utils.RxSchedulersFacade
import com.jakdor.geosave.utils.StringUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class PictureStorageRepository @Inject constructor(private val firebaseStorage: FirebaseStorage,
                                                   private val reposRepository: ReposRepository,
                                                   private val schedulers: RxSchedulersFacade) {

    val pictureUploadStatus: BehaviorSubject<RequestStatus> = BehaviorSubject.create()

    enum class RequestStatus {
        IDLE, READY, ONGOING, ERROR, NO_NETWORK
    }

    init {
        pictureUploadStatus.onNext(RequestStatus.IDLE)
    }

    /**
     * Upload repository picture to Firebase Storage
     * //todo file compression
     */
    fun uploadRepositoryPicture(repo: Repo, repoIndex: Int, picFile: File){
        pictureUploadStatus.onNext(RequestStatus.ONGOING)

        val picRef = firebaseStorage.reference.child(repo.ownerUid +
                "/" + StringUtils.randomString(20) + ".jpg")

        val uploadTask = picRef.putFile(Uri.fromFile(picFile))
        uploadTask.addOnSuccessListener {
            picRef.downloadUrl.addOnSuccessListener { url ->
                if(url.encodedPath != null) reposRepository.updateRepoPicUrl(repoIndex, url.toString())
            }

            val disposable = CompositeDisposable()
            disposable.add(reposRepository.picUpdateStatusStream
                    .subscribeOn(schedulers.io())
                    .observeOn(schedulers.io())
                    .subscribe(
                            { result ->
                                when(result){
                                    true -> {
                                        pictureUploadStatus.onNext(RequestStatus.READY)
                                        pictureUploadStatus.onNext(RequestStatus.IDLE)
                                    }
                                    false -> {
                                        pictureUploadStatus.onNext(RequestStatus.ERROR)
                                        pictureUploadStatus.onNext(RequestStatus.IDLE)
                                    }
                                }
                                disposable.clear()
                            },
                            { e ->
                                Timber.e("Error observing picUpdateStatusStream: %s",
                                        e.toString())
                                pictureUploadStatus.onNext(RequestStatus.ERROR)
                                pictureUploadStatus.onNext(RequestStatus.IDLE)
                                disposable.clear()
                            }
                    ))
        }
        uploadTask.addOnFailureListener {
            pictureUploadStatus.onNext(RequestStatus.ERROR)
            pictureUploadStatus.onNext(RequestStatus.IDLE)
        }
    }
}