/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.di

import com.jakdor.geosave.ui.main.MainActivity
import com.jakdor.geosave.ui.main.MainModule
import com.jakdor.geosave.ui.main.MainViewModule
import com.jakdor.geosave.ui.splash.SplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * App sub-components binding module
 */
@Module
abstract class BuildersModule {

    @ContributesAndroidInjector(modules = [
        MainModule::class,
        MainViewModule::class,
        MainActivityMVVMFragmentBuilderModule::class])
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [MainModule::class])
    abstract fun bindSplashActivity(): SplashActivity
}