
package com.jakdor.geosave.common.model;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ElevationApi implements Serializable {

    @SerializedName("elevationApiResults")
    @Expose
    private List<ElevationApiResult> elevationApiResults = null;
    private final static long serialVersionUID = 8255691863523063062L;

    /**
     * No args constructor for use in serialization
     */
    public ElevationApi() {
    }

    public ElevationApi(List<ElevationApiResult> elevationApiResults) {
        super();
        this.elevationApiResults = elevationApiResults;
    }

    public List<ElevationApiResult> getElevationApiResults() {
        return elevationApiResults;
    }

    public void setElevationApiResults(List<ElevationApiResult> elevationApiResults) {
        this.elevationApiResults = elevationApiResults;
    }
}
