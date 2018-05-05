package com.jakdor.geosave.di

import com.jakdor.geosave.main.MainActivity
import com.jakdor.geosave.main.MainModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * App sub-components binding module
 */
@Module
abstract class BuildersModule {

    @ContributesAndroidInjector(modules = [MainModule::class])
    abstract fun bindMainActivity(): MainActivity

}