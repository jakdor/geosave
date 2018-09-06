package com.jakdor.geosave.di

import android.app.Application
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.jakdor.geosave.App
import com.jakdor.geosave.arch.ViewModelFactory
import com.jakdor.geosave.common.network.RetrofitFactory
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.common.repository.RestApiRepository
import com.jakdor.geosave.common.repository.SharedPreferencesRepository
import com.jakdor.geosave.common.wrapper.FirebaseAuthWrapper
import com.jakdor.geosave.utils.RxSchedulersFacade
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide dependencies injections
 */
@Module(subcomponents = [ViewModelSubComponent::class])
class AppModule {

    @Provides
    fun provideContext(app: App): Context{
        return app.applicationContext
    }

    @Singleton
    @Provides
    fun provideViewModelFactory(
            viewModelBuilder: ViewModelSubComponent.Builder): ViewModelProvider.Factory {
        return ViewModelFactory(viewModelBuilder.build())
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

    @Singleton
    @Provides
    fun provideSharedPreferencesRepository(app: Application): SharedPreferencesRepository {
        return SharedPreferencesRepository(app.applicationContext)
    }

    @Provides
    fun provideFirebaseAuthWrapper(): FirebaseAuthWrapper{
        return FirebaseAuthWrapper(FirebaseAuth.getInstance())
    }

    @Singleton
    @Provides
    fun provideRestApiRepository(): RestApiRepository{
        return RestApiRepository(RetrofitFactory())
    }
}