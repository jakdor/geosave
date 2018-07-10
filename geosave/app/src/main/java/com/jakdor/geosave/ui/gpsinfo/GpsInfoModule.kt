package com.jakdor.geosave.ui.gpsinfo

import com.jakdor.geosave.common.repository.GpsInfoRepository
import dagger.Module
import dagger.Provides

@Module
class GpsInfoModule {

    @Provides
    fun provideGpsInfoPresenter(gpsInfoView: GpsInfoContract.GpsInfoView,
                                gpsInfoRepository: GpsInfoRepository): GpsInfoPresenter{
        return GpsInfoPresenter(gpsInfoView, gpsInfoRepository)
    }
}