package com.jakdor.geosave.ui.main

import android.app.Application
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.common.repository.ShareMessageFormatter
import com.jakdor.geosave.common.repository.SharedPreferencesRepository
import com.jakdor.geosave.common.wrapper.FirebaseAuthWrapper
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @Provides
    fun provideMainPresenter(mainView: MainContract.MainView,
                             gpsInfoRepository: GpsInfoRepository,
                             firebaseAuthWrapper: FirebaseAuthWrapper,
                             shareMessageFormatter: ShareMessageFormatter): MainPresenter {
        return MainPresenter(mainView, gpsInfoRepository, firebaseAuthWrapper, shareMessageFormatter)
    }

    @Provides
    fun provideGoogleApiClient(mainActivity: MainActivity): GoogleApiClient {
        return GoogleApiClient.Builder(mainActivity)
                .enableAutoManage(mainActivity, 0, mainActivity)
                .addConnectionCallbacks(mainActivity)
                .addOnConnectionFailedListener(mainActivity)
                .addApi(LocationServices.API)
                .build()
    }

    @Provides
    fun provideShareMessageFormatter(app: Application,
                                     sharedPreferencesRepository: SharedPreferencesRepository):
            ShareMessageFormatter {
        return ShareMessageFormatter(app.applicationContext, sharedPreferencesRepository)
    }
}