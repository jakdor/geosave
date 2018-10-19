/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.main

import android.app.Application
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.jakdor.geosave.common.repository.*
import com.jakdor.geosave.common.wrapper.FirebaseAuthWrapper
import com.jakdor.geosave.utils.RxSchedulersFacade
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @Provides
    fun provideMainPresenter(mainView: MainContract.MainView,
                             gpsInfoRepository: GpsInfoRepository,
                             firebaseAuthWrapper: FirebaseAuthWrapper,
                             shareMessageFormatter: ShareMessageFormatter,
                             cameraRepository: CameraRepository,
                             resposRepository: ReposRepository,
                             schedulersFacade: RxSchedulersFacade): MainPresenter {
        return MainPresenter(mainView, gpsInfoRepository, firebaseAuthWrapper,
                shareMessageFormatter, cameraRepository, resposRepository, schedulersFacade)
    }

    @Provides
    fun provideGoogleApiClient(app: Application, mainActivity: MainActivity): GoogleApiClient {
        return GoogleApiClient.Builder(app)
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