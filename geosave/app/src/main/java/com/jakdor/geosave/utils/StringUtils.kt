/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.utils

import java.util.*

internal object StringUtils {

    /**
     * Crates random Strings for use as crude guid
     */
    fun randomString(length: Int): String {
        val leftLimit: Int = 'A'.toInt()
        val rightLimit: Int = 'z'.toInt()
        val random = Random()
        val buffer = StringBuilder(length)
        for (i in 0 until length) {
            val randomLimitedInt
                    = leftLimit + (random.nextFloat() * (rightLimit - leftLimit + 1)).toInt()
            buffer.append(randomLimitedInt.toChar())
        }
        return buffer.toString()
    }
}
