package com.jakdor.geosave.service.gps

import dagger.Module
import dagger.Provides

@Module
class GpsServiceModule {

    @Provides
    fun provideGpsListenerService(): GpsListenerService{
        return GpsListenerService()
    }
}