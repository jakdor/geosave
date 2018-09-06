package com.jakdor.geosave.common.repository

import com.jakdor.geosave.common.model.ElevationApi
import com.jakdor.geosave.common.network.ElevationApiService
import com.jakdor.geosave.common.network.RetrofitFactory
import io.reactivex.Observable
import javax.inject.Inject

class RestApiRepository @Inject constructor(retrofitFactory: RetrofitFactory){

    private val elevationApiService: ElevationApiService = retrofitFactory.createService(
            ElevationApiService.API_URL, ElevationApiService::class.java)

    /**
     * Call open elevation api with single location query
     * @param lat latitude decimal format
     * @param long Longitude decimal format
     * @return [Observable] with [ElevationApi]
     */
    fun getElevationApi(lat: Double, long: Double): Observable<ElevationApi>{
        return elevationApiService.getElevationApi(String.format("%f,%f", lat, long))
    }
}