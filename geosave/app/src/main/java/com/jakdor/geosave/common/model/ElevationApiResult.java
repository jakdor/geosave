
package com.jakdor.geosave.common.model;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ElevationApiResult implements Serializable {

    @SerializedName("latitude")
    @Expose
    private Double latitude;
    @SerializedName("elevation")
    @Expose
    private Integer elevation;
    @SerializedName("longitude")
    @Expose
    private Double longitude;
    private final static long serialVersionUID = 4139053364619510455L;

    /**
     * No args constructor for use in serialization
     */
    public ElevationApiResult() {
    }

    public ElevationApiResult(Double latitude, Integer elevation, Double longitude) {
        super();
        this.latitude = latitude;
        this.elevation = elevation;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getElevation() {
        return elevation;
    }

    public void setElevation(Integer elevation) {
        this.elevation = elevation;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
