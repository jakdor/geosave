package com.jakdor.geosave.di

import com.jakdor.geosave.service.gps.GpsService
import com.jakdor.geosave.service.gps.GpsServiceModule
import com.jakdor.geosave.ui.main.*
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
        GpsInfoFragmentProvider::class])
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [GpsServiceModule::class])
    abstract fun bindGpsService(): GpsService
}