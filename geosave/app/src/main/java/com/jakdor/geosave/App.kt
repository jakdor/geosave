package com.jakdor.geosave

import android.app.Activity
import android.app.Application
import com.jakdor.geosave.di.AppInjector
import com.jakdor.geosave.utils.AppLogger
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class App : Application(), HasActivityInjector{

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun activityInjector(): DispatchingAndroidInjector<Activity> {
        return dispatchingAndroidInjector
    }

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
        AppLogger.init(this)
    }
}