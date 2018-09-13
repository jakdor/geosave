/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.common.model

import android.location.Location

/**
 * Location data model - platform independent geo data structure
 */
data class UserLocation(val latitude: Double, val longitude: Double, var altitude: Double,
                        val accuracy: Float, val provider: String, val speed: Float,
                        val bearing: Float, var elevationApi: Boolean){
    constructor(location: Location): this(location.latitude, location.longitude, location.altitude,
            location.accuracy, location.provider, location.speed, location.bearing, false){
        if(altitude == 0.0){ //exact 0.0 can't occur with GPS provider, invalidate value
            altitude = -999.0
        }
    }
}