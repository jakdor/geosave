package com.jakdor.geosave

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
