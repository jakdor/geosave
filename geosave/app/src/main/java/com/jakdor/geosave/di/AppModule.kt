package com.jakdor.geosave.di

import android.content.Context
import com.jakdor.geosave.App
import com.jakdor.geosave.common.repository.GpsInfoRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * App-wide dependencies injections
 */
@Module
class AppModule {

    @Provides
    fun provideContext(app: App): Context{
        return app.applicationContext
    }

    @Singleton
    @Provides
    fun provideGpsInfoRepository(): GpsInfoRepository {
        return GpsInfoRepository()
    }
}