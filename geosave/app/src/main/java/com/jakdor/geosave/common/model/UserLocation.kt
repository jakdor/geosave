package com.jakdor.geosave.common.model

import android.location.Location

/**
 * Location data model - platform independent geo data structure
 */
data class UserLocation(val latitude: Double, val longitude: Double, var altitude: Double,
                        val accuracy: Float, val provider: String, val speed: Float,
                        val bearing: Float){
    constructor(location: Location): this(location.latitude, location.longitude, location.altitude,
            location.accuracy, location.provider, location.speed, location.bearing){
        if(altitude == 0.0){ //exact 0.0 can't occur with GPS provider, invalidate value
            altitude = -999.0
        }
    }
}