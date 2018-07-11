package com.jakdor.geosave.ui.gpsinfo

interface GpsInfoContract {

    interface GpsInfoView{
        fun setPositionTitleTextView(resId: Int)
        fun setPositionFieldTextView(posStr: String)
        fun setAltitudeTileTextView(resId: Int)
        fun setAltitudeFieldTextView(altStr: String)
    }

    interface GpsInfoPresenter
}