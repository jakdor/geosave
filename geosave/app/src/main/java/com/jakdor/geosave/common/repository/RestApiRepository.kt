/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.common.repository

import android.content.Context
import android.net.ConnectivityManager
import com.jakdor.geosave.common.model.ElevationApi
import com.jakdor.geosave.common.network.ElevationApiService
import com.jakdor.geosave.common.network.RetrofitFactory
import io.reactivex.Observable
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class RestApiRepository @Inject constructor(retrofitFactory: RetrofitFactory){

    private val elevationApiService: ElevationApiService = retrofitFactory.createService(
            ElevationApiService.API_URL, ElevationApiService::class.java)

    /**
     * Check network status
     * @param context required to retrieve ConnectivityService
     * @return boolean - network status
     */
    fun checkNetworkStatus(context: Context): Boolean {
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo
        return if (networkInfo == null) {
            Timber.e("Internet status: no service")
            false
        } else {
            Timber.i("Internet status: OK")
            true
        }
    }

    /**
     * Call open elevation api with single location query
     * @param lat latitude decimal format
     * @param long Longitude decimal format
     * @return [Observable] with [ElevationApi]
     */
    fun getElevationApi(lat: Double, long: Double): Observable<ElevationApi>{
        return elevationApiService.getElevationApi(String.format(Locale.US, "%f,%f", lat, long))
    }
}