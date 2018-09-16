/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave

import android.app.Activity
import android.app.Application
import com.jakdor.geosave.di.AppInjector
import com.jakdor.geosave.utils.AppLogger
import com.squareup.leakcanary.AndroidExcludedRefs
import com.squareup.leakcanary.LeakCanary
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
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }

        leakCanary()
        AppInjector.init(this)
        AppLogger.init(this)
    }

    private fun leakCanary(){
        //exclude known libs leaks
        val excludedRefs = AndroidExcludedRefs.createAndroidDefaults()
                .instanceField("android.view.inputmethod.InputMethodManager",
                        "mCurRootView")
                .instanceField("android.view.inputmethod.InputMethodManager",
                        "mNextServedView")
                .instanceField("android.view.inputmethod.InputMethodManager",
                        "mServedView")
                .build()

        LeakCanary.refWatcher(this).excludedRefs(excludedRefs).buildAndInstall()
    }
}