package com.jakdor.geosave.gpsinfo

import dagger.Binds
import dagger.Module

@Module
abstract class GpsInfoViewModule {

    @Binds
    abstract fun provideGpsInfoView(gpsInfoFragment: GpsInfoFragment): GpsInfoContract.GpsInfoView
}