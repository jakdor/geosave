/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.common.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ElevationApiResult
constructor(@SerializedName("latitude") @Expose var latitude: Double?,
            @SerializedName("elevation") @Expose var elevation: Int?,
            @SerializedName("longitude") @Expose var longitude: Double?)
