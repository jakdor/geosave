/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.di

import com.jakdor.geosave.ui.gpsinfo.GpsInfoFragment
import com.jakdor.geosave.ui.locations.LocationsFragment
import com.jakdor.geosave.ui.map.MapFragment
import com.jakdor.geosave.ui.preferences.PreferencesFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * MainActivity fragment injection point
 */
@Module
abstract class MainActivityMVVMFragmentBuilderModule {
    @ContributesAndroidInjector
    abstract fun contributeGpsInfoFragment(): GpsInfoFragment

    @ContributesAndroidInjector
    abstract fun contributeMapFragment(): MapFragment

    @ContributesAndroidInjector
    abstract fun contributePreferencesFragment(): PreferencesFragment

    @ContributesAndroidInjector
    abstract fun contributeLocationsFragmnet(): LocationsFragment
}
