package com.jakdor.geosave.ui.main

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @Provides
    fun provideMainPresenter(mainView: MainContract.MainView): MainPresenter {
        return MainPresenter(mainView)
    }

    @Provides
    fun provideGoogleApiClient(mainActivity: MainActivity): GoogleApiClient{
        return GoogleApiClient.Builder(mainActivity)
                .enableAutoManage(mainActivity, 0, mainActivity)
                .addConnectionCallbacks(mainActivity)
                .addOnConnectionFailedListener(mainActivity)
                .addApi(LocationServices.API)
                .build()
    }
}