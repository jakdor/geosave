/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.common.network

import com.jakdor.geosave.common.model.ElevationApi
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ElevationApiService {

    @GET("lookup")
    fun getElevationApi(@Query("locations", encoded = true) latLong: String): Observable<ElevationApi>

    companion object {
        const val API_URL = "https://api.open-elevation.com/api/v1/"
    }
}