package com.jakdor.geosave.main

import com.jakdor.geosave.gpsinfo.GpsInfoFragment
import com.jakdor.geosave.gpsinfo.GpsInfoModule
import com.jakdor.geosave.gpsinfo.GpsInfoViewModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class GpsInfoFragmentProvider {

    @ContributesAndroidInjector(modules = [
        GpsInfoModule::class,
        GpsInfoViewModule::class])
    abstract fun bindGpsInfoFragment(): GpsInfoFragment
}