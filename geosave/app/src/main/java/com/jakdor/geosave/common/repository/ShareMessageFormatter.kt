package com.jakdor.geosave.common.repository

import android.content.Context
import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.UserLocation
import javax.inject.Inject

class ShareMessageFormatter @Inject constructor(
        private val context: Context,
        private val sharedPreferencesRepository: SharedPreferencesRepository){

    /**
     * Returns share message based on user preferences form [SharedPreferencesRepository]
     * and provided [UserLocation] object
     */
    fun build(location: UserLocation): String {

        //location
        var msg: String = context.getString(R.string.location_title) + ": "
        when(sharedPreferencesRepository.getString(
                SharedPreferencesRepository.locationUnits, "0").toInt()){
            0 -> { //decimal
                msg += LocationConverter.decimalFormat(location.latitude, location.longitude)
            }
            1 -> { //sexigesimal
                msg += LocationConverter.dmsFormat(location.latitude, location.longitude)
            }
            2 -> { //decimal degrees
                msg += LocationConverter.decimalDegreesFormat(location.latitude, location.longitude)
            }
            3 -> { //degrees decimal minutes
                msg += LocationConverter.dmFormat(location.latitude, location.longitude)
            }
        }

        //return abridged
        if(!sharedPreferencesRepository.getBoolean(
                        SharedPreferencesRepository.shareFull, false)){
            return msg
        }

        //altitude
        msg += "\n" + context.getString(R.string.altitude_title) + ": "
        if(location.altitude != -999.0){
            when(sharedPreferencesRepository.getString(
                    SharedPreferencesRepository.altUnits, "0").toInt()) {
                0 -> { //meters
                    msg += String.format("%.2f m", location.altitude)
                    msg += addAltitudeAccuracy(location, 1.0)
                    msg += " m"
                }
                1 -> { //kilometers
                    msg += String.format("%.4f km", location.altitude / 1000.0)
                    msg += addAltitudeAccuracy(location, 0.001)
                    msg += " km"
                }
                2 -> { //feats
                    msg += String.format("%.2f ft", location.altitude * 3.2808399)
                    msg += addAltitudeAccuracy(location, 3.2808399)
                    msg += " ft"
                }
                3 -> { //land miles
                    msg += String.format("%.6f mi", location.altitude * 0.000621371192)
                    msg += addAltitudeAccuracy(location, 0.000621371192)
                    msg += " mi"
                }
            }
        }

        //accuracy
        msg += "\n" + context.getString(R.string.accuracy_title) + ": "
        when(sharedPreferencesRepository.getString(
                SharedPreferencesRepository.accUnits, "0").toInt()){
            0 -> { //meters
                msg += String.format("%.2f m", location.accuracy)
            }
            1 -> { //kilometers
                msg += String.format("%.4f km", location.accuracy / 1000.0)
            }
            2 -> { //feats
                msg += String.format("%.2f ft", location.accuracy * 3.2808399)
            }
            3 -> { //land miles
                msg += String.format("%.6f mi", location.accuracy * 0.000621371192)
            }
            4 -> { //nautical miles
                msg += String.format("%.6f nmi", location.accuracy * 0.000539956803)
            }
        }

        //speed
        msg += "\n" + context.getString(R.string.speed_title) + ": "
        when(sharedPreferencesRepository.getString(
                SharedPreferencesRepository.speedUnits, "0").toInt()){
            0 -> { //m/s
                msg += String.format("%.2f m/s", location.speed)
            }
            1 -> { //km/h
                msg += String.format("%.2f km/h", location.speed * 3.6)
            }
            2 -> { //ft/s
                msg += String.format("%.2f ft/s", location.speed * 3.2808399)
            }
            3 -> { //mph
                msg += String.format("%.2f mph", location.speed * 2.23693629)
            }
        }
        
        return msg
    }

    /**
     * Adds altitude accuracy according to provider and used units
     * @multiplier units multiple form meters to target units
     */
    private fun addAltitudeAccuracy(location: UserLocation, multiplier: Double): String {
        var str = " \u00B1 "
        str += if(location.elevationApi){ //api accuracy
            if(multiplier == 3.2808399) String.format("%.2f", 45.0)
            else String.format("%.2f", 15 * multiplier)
        } else { //gps accuracy
            String.format("%.2f", location.accuracy * 2.5)
        }

        return str
    }
}