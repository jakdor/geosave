package com.jakdor.geosave

import com.jakdor.geosave.di.DaggerAppComponent
import com.jakdor.geosave.utils.AppLogger
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class App : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        val appComponent = DaggerAppComponent.builder().application(this).build()
        appComponent.inject(this)
        return appComponent
    }

    override fun onCreate() {
        super.onCreate()
        AppLogger.init(this)
    }
}