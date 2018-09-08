package com.jakdor.geosave.common.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ElevationApiResult
constructor(@SerializedName("latitude") @Expose var latitude: Double?,
            @SerializedName("elevation") @Expose var elevation: Int?,
            @SerializedName("longitude") @Expose var longitude: Double?)
