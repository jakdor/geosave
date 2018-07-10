package com.jakdor.geosave.ui.gpsinfo

interface GpsInfoContract {

    interface GpsInfoView{
        fun setPositionTextView(posStr: String)
        fun setPositionTextView(resId: Int)
    }

    interface GpsInfoPresenter
}