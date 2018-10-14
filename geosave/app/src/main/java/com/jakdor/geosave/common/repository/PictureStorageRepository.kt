/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.common.repository

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.utils.RxSchedulersFacade
import com.jakdor.geosave.utils.StringUtils
import id.zelory.compressor.Compressor
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import java.io.ByteArrayOutputStream


class PictureStorageRepository @Inject constructor(private val firebaseStorage: FirebaseStorage,
                                                   private val reposRepository: ReposRepository,
                                                   private val schedulers: RxSchedulersFacade,
                                                   private val context: Application) {

    val pictureUploadStatus: BehaviorSubject<RequestStatus> = BehaviorSubject.create()

    enum class RequestStatus {
        IDLE, READY, ONGOING, ERROR, NO_NETWORK
    }

    init {
        pictureUploadStatus.onNext(RequestStatus.IDLE)
    }

    /**
     * Compress picture file asynchronously
     */
    fun compressPicture(picFile: File): Flowable<Bitmap>{
        return Compressor(context)
                .setMaxHeight(240)
                .setMaxWidth(240)
                .compressToBitmapAsFlowable(picFile)
                .subscribeOn(schedulers.computation())
                .observeOn(schedulers.io())
    }

    /**
     * Upload repository picture to Firebase Storage from file
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
            observePicUpdateStatus()
        }
        uploadTask.addOnFailureListener {
            pictureUploadStatus.onNext(RequestStatus.ERROR)
            pictureUploadStatus.onNext(RequestStatus.IDLE)
        }
    }

    /**
     * Upload repository picture to Firebase Storage from bitmap
     */
    fun uploadRepositoryPicture(repo: Repo, repoIndex: Int, picBitmap: Bitmap){
        pictureUploadStatus.onNext(RequestStatus.ONGOING)

        val picRef = firebaseStorage.reference.child(repo.ownerUid +
                "/" + StringUtils.randomString(20) + ".jpg")

        val stream = ByteArrayOutputStream()
        picBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        val byteArray = stream.toByteArray()
        picBitmap.recycle()

        val uploadTask = picRef.putBytes(byteArray)
        uploadTask.addOnSuccessListener {
            picRef.downloadUrl.addOnSuccessListener { url ->
                if(url.encodedPath != null) reposRepository.updateRepoPicUrl(repoIndex, url.toString())
            }
            observePicUpdateStatus()
        }
        uploadTask.addOnFailureListener {
            pictureUploadStatus.onNext(RequestStatus.ERROR)
            pictureUploadStatus.onNext(RequestStatus.IDLE)
        }
    }

    /**
     * Observe [ReposRepository] picUpdateStatusStream
     */
    private fun observePicUpdateStatus(){
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
}