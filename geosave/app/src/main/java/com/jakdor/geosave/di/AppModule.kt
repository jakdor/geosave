/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.di

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jakdor.geosave.App
import com.jakdor.geosave.arch.ViewModelFactory
import com.jakdor.geosave.common.network.RetrofitFactory
import com.jakdor.geosave.common.repository.*
import com.jakdor.geosave.common.wrapper.FirebaseAuthWrapper
import com.jakdor.geosave.utils.RxSchedulersFacade
import dagger.Module
import dagger.Provides
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
    fun provideGpsInfoRepository(app: Application, restApiRepository: RestApiRepository,
            sharedPreferencesRepository: SharedPreferencesRepository): GpsInfoRepository {
        return GpsInfoRepository(app.applicationContext, provideRxSchedulersFacade(),
                restApiRepository, sharedPreferencesRepository)
    }

    @Singleton
    @Provides
    fun provideSharedPreferencesRepository(app: Application): SharedPreferencesRepository {
        return SharedPreferencesRepository(app.applicationContext)
    }

    @Singleton
    @Provides
    fun provideFirebaseAuthWrapper(firestore: FirebaseFirestore): FirebaseAuthWrapper{
        return FirebaseAuthWrapper(FirebaseAuth.getInstance(), firestore)
    }

    @Singleton
    @Provides
    fun provideRestApiRepository(): RestApiRepository{
        return RestApiRepository(RetrofitFactory())
    }

    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore{
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideReposRepository(schedulers: RxSchedulersFacade,
                               firebaseAuthWrapper: FirebaseAuthWrapper,
                               firebaseFirestore: FirebaseFirestore): ReposRepository{
        return ReposRepository(schedulers, firebaseAuthWrapper, firebaseFirestore)
    }

    @Provides
    fun provideUserRepository(firebaseFirestore: FirebaseFirestore): UserRepository{
        return UserRepository(firebaseFirestore)
    }

    @Singleton
    @Provides
    fun provideCameraRepository(): CameraRepository {
        return CameraRepository()
    }

    @Provides
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    fun providePictureStorageRepository(storage: FirebaseStorage,
                                        reposRepository: ReposRepository,
                                        schedulers: RxSchedulersFacade,
                                        app: Application): PictureStorageRepository {
        return PictureStorageRepository(storage, reposRepository, schedulers, app)
    }
}