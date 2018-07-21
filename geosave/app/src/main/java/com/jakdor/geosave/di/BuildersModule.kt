package com.jakdor.geosave.di

import com.jakdor.geosave.ui.main.GpsInfoFragmentProvider
import com.jakdor.geosave.ui.main.MainActivity
import com.jakdor.geosave.ui.main.MainModule
import com.jakdor.geosave.ui.main.MainViewModule
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
        GpsInfoFragmentProvider::class,
        MainActivityMVVMFragmentBuilderModule::class])
    abstract fun bindMainActivity(): MainActivity

}