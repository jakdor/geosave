package com.jakdor.geosave.di

import com.jakdor.geosave.ui.gpsinfo.GpsInfoFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * MainActivity fragment injection point
 */
@Module
abstract class MainActivityMVVMFragmentBuilderModule {
    @ContributesAndroidInjector
    abstract fun contributeGpsInfoFragment(): GpsInfoFragment
}
