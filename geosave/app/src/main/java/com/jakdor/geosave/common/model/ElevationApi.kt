package com.jakdor.geosave.common.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ElevationApi
constructor(@SerializedName("results")
            @Expose var elevationApiResults: List<ElevationApiResult>)
