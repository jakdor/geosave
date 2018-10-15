/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.common.repository

import io.reactivex.subjects.PublishSubject
import java.io.File

/**
 * Allows for communication between different viewModels to MainPresenter and MainActivity
 * requesting Camera/photos access
 */
class CameraRepository {

    val cameraRequest: PublishSubject<CameraRequestInfo> = PublishSubject.create()
    val cameraResult: PublishSubject<CameraRequestResult> = PublishSubject.create()

    /**
     * Forward request to cameraRequest Subject
     * @tag caller tag
     * @cameraFeature type of camera feature
     */
    fun requestCameraPhotos(tag: String, cameraFeature: CameraFeature){
        cameraRequest.onNext(CameraRequestInfo(tag, cameraFeature))
    }

    /**
     * Return request result
     * @tag caller tag
     * @file handle to picture file
     */
    fun onCameraResult(tag: String, file: File){
        cameraResult.onNext(CameraRequestResult(tag, file))
    }

    data class CameraRequestInfo
    constructor(val tag: String, val feature: CameraFeature)

    data class CameraRequestResult
    constructor(val tag: String, val file: File)

    enum class CameraFeature {
        TAKE_PHOTO, GET_GALLERY, GET_DOCUMENTS
    }
}