package com.jakdor.geosave.ui.gpsinfo

interface GpsInfoContract {

    interface GpsInfoView{
        fun setPositionTitle(resId: Int)
        fun setPositionField(posStr: String)
        fun setAltitudeTile(resId: Int)
        fun setAltitudeField(altStr: String)
        fun setAccuracyTitle(resId: Int)
        fun setAccuracyField(accStr: String)
        fun setSpeedTitle(resId: Int)
        fun setSpeedField(speedStr: String)
        fun setBearingTitle(resId: Int)
        fun setBearingField(bearStr: String)
        fun setProviderTitle(resId: Int)
        fun setProviderField(resId: Int)
    }

    interface GpsInfoPresenter
}