package com.jakdor.geosave.di

import android.content.Context
import com.jakdor.geosave.App
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.utils.RxSchedulersFacade
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

    @Provides
    fun provideRxSchedulersFacade(): RxSchedulersFacade {
        return RxSchedulersFacade()
    }

    @Singleton
    @Provides
    fun provideGpsInfoRepository(): GpsInfoRepository {
        return GpsInfoRepository(provideRxSchedulersFacade())
    }
}