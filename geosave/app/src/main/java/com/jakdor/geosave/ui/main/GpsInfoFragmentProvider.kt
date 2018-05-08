package com.jakdor.geosave.ui.main

import com.jakdor.geosave.ui.gpsinfo.GpsInfoFragment
import com.jakdor.geosave.ui.gpsinfo.GpsInfoModule
import com.jakdor.geosave.ui.gpsinfo.GpsInfoViewModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class GpsInfoFragmentProvider {

    @ContributesAndroidInjector(modules = [
        GpsInfoModule::class,
        GpsInfoViewModule::class])
    abstract fun bindGpsInfoFragment(): GpsInfoFragment
}