/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.utils

import java.util.Random

internal object TestUtils {

    /**
     * Crates random Strings for tests
     * @return String, length 0-50 chars A-z
     */
    fun randomString(): String {
        val leftLimit: Int = 'A'.toInt()
        val rightLimit: Int = 'z'.toInt()
        val random = Random()
        val targetStringLength = random.nextInt(50)
        val buffer = StringBuilder(targetStringLength)
        for (i in 0 until targetStringLength) {
            val randomLimitedInt
                    = leftLimit + (random.nextFloat() * (rightLimit - leftLimit + 1)).toInt()
            buffer.append(randomLimitedInt.toChar())
        }
        return buffer.toString()
    }
}
