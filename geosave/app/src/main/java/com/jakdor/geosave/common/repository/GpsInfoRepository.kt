package com.jakdor.geosave.common.repository

import timber.log.Timber
import javax.inject.Singleton

@Singleton
class GpsInfoRepository{

    var count: Int = 0

    fun test(){
        Timber.i("test: %d", count++)
    }
}