package com.jakdor.geosave.ui.gpsinfo

import dagger.Binds
import dagger.Module

@Module
abstract class GpsInfoViewModule {

    @Binds
    abstract fun bindGpsInfoView(gpsInfoFragment: GpsInfoFragment): GpsInfoContract.GpsInfoView
}