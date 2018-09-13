/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.common.repository

import java.util.*

internal object LocationConverter{

    /**
     * Format decimal string
     */
    fun decimalFormat(lat: Double, long: Double): String {
        return String.format(Locale.US, "%f, %f", lat, long)
    }

    /**
     * Format DMS string
     */
    fun dmsFormat(lat: Double, long: Double): String {
        val latSign = if(lat >= 0) "N" else "S"
        val longSign = if(long >= 0) "E" else "W"
        return dmsConvert(Math.abs(lat)) + latSign +
                " " + dmsConvert(Math.abs(long)) + longSign
    }

    /**
     * Format decimal degrees string
     */
    fun decimalDegreesFormat(lat: Double, long: Double): String {
        val latSign = if(lat >= 0) "N" else "S"
        val longSign = if(long >= 0) "E" else "W"
        return String.format(Locale.US, "%f\u00b0 %s %f\u00b0 %s",
                Math.abs(lat), latSign,  Math.abs(long), longSign)
    }

    /**
     * Format degrees decimal minutes string
     */
    fun dmFormat(lat: Double, long: Double): String {
        val latSign = if(lat >= 0) "N" else "S"
        val longSign = if(long >= 0) "E" else "W"
        return dmConvert(Math.abs(lat)) + " " + latSign +
                " " + dmConvert(Math.abs(long)) + " " + longSign
    }

    /**
     * Convert decimal to sexigesimal(DMS) location string
     */
    private fun dmsConvert(loc: Double): String {
        val d = Math.floor(loc)
        var m = (loc - d) * 60
        val s = (m - Math.floor(m)) * 60
        m = Math.floor(m)
        return String.format(Locale.US, "%.0f\u00b0%.0f\u0027%.1f\u0022", d, m, s)
    }

    /**
     * Convert decimal to DM location string
     */
    private fun dmConvert(loc: Double): String {
        val d = Math.floor(loc)
        val m = (loc - d) * 60
        return String.format(Locale.US, "%.0f\u00b0%.4f\u0027", d, m)
    }
}