package com.jakdor.geosave.ui.main

import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @Provides
    fun provideMainPresenter(mainView: MainContract.MainView): MainPresenter {
        return MainPresenter(mainView)
    }
}