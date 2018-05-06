package com.jakdor.geosave.gpsinfo

import dagger.Module
import dagger.Provides

@Module
class GpsInfoModule {

    @Provides
    fun provideGpsInfoPresenter(gpsInfoView: GpsInfoContract.GpsInfoView): GpsInfoPresenter{
        return GpsInfoPresenter(gpsInfoView)
    }
}