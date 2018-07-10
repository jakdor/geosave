package com.jakdor.geosave.common.model

import android.location.Location

/**
 * Location data model - platform independent geo data structure
 */
data class UserLocation(val latitude: Double, val longitude: Double, val altitude: Double,
                        val accuracy: Float, val provider: String, val speed: Float){
    constructor(location: Location): this(location.latitude, location.longitude, location.altitude,
            location.accuracy, location.provider, location.speed)
}