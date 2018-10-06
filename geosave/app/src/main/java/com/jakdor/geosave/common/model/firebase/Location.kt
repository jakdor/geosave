/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.common.model.firebase

/**
 * Firebase location object
 */
data class Location
constructor(var name: String = "",
            var authorUid: String = "",
            var info: String = "",
            var picUrl: String = "",
            var latitude: Double = 0.0,
            var longitude: Double = 0.0,
            var altitude: Double = 0.0,
            var accuracy: Float = 0.0f,
            var accuracyRange: Float = 0.0f)