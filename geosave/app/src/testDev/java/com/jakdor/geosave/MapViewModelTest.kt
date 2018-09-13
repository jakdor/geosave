/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.common.repository.RestApiRepository
import com.jakdor.geosave.common.repository.SharedPreferencesRepository
import com.jakdor.geosave.ui.map.MapViewModel
import com.jakdor.geosave.utils.RxSchedulersFacade
import com.jakdor.geosave.utils.TestUtils
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.util.*

class MapViewModelTest {
    
    @get:Rule
    private val thrownRule = ExpectedException.none()
    
    @get:Rule
    var instantExecRule = InstantTaskExecutorRule()

    private val testSavedInt = Random().nextInt()
    private val testSavedInt2 = Random().nextInt()
    
    private val app = mock<Application>()
    private val rxSchedulersFacade = mock<RxSchedulersFacade> { 
        on { computation() }.thenReturn(Schedulers.computation())
    }
    private val gpsInfoRepository = mock<GpsInfoRepository> {
        on { subscribe(any()) }.thenReturn(com.nhaarman.mockito_kotlin.mock())
    }
    private val restApiRepository = mock<RestApiRepository>()
    private val sharedPreferencesRepository = mock<SharedPreferencesRepository>{
        on { getInt(SharedPreferencesRepository.mapTypeKey, 0) }.thenReturn(testSavedInt)
        on { getString(SharedPreferencesRepository.locationUnits, "0") }
                .thenReturn(testSavedInt2.toString())
    }

    private var mapViewModel = MapViewModel(
            app, rxSchedulersFacade, gpsInfoRepository, sharedPreferencesRepository)

    /**
     * Test if new value of map type id is forwarded to mapType stream
     * and saved by SharedPreferencesRepository
     */
    @Test
    fun onMapTypeClickedNewValueTest(){
        val mapId = Random().nextInt(3)

        mapViewModel.onMapTypeClicked(mapId)

        Assert.assertNotNull(mapViewModel.mapType.value)
        Assert.assertEquals(mapId, mapViewModel.mapType.value!!)
        verify(sharedPreferencesRepository).save(SharedPreferencesRepository.mapTypeKey, mapId)
    }

    /**
     * Test if old value of map type won't be forwarded and saved again
     */
    @Test
    fun onMapTypeClickedOldValueTest(){
        val mapId = Random().nextInt(3)

        mapViewModel.onMapTypeClicked(mapId)
        mapViewModel.onMapTypeClicked(mapId)

        Assert.assertNotNull(mapViewModel.mapType.value)
        Assert.assertEquals(mapId, mapViewModel.mapType.value!!)
        verify(sharedPreferencesRepository, times(1))
                .save(SharedPreferencesRepository.mapTypeKey, mapId)
    }

    /**
     * Test load saved values from [SharedPreferencesRepository]
     */
    @Test
    fun loadPreferencesTest(){
        mapViewModel.loadPreferences()

        Assert.assertNotNull(mapViewModel.mapType.value)
        Assert.assertEquals(testSavedInt, mapViewModel.mapType.value!!)
        Assert.assertNotNull(mapViewModel.locationType.value)
        Assert.assertEquals(testSavedInt2, mapViewModel.locationType.value!!)
    }

    /**
     * Test subscribe to [GpsInfoRepository] stream
     */
    @Test
    fun requestUserLocationUpdatesTest(){
        mapViewModel.requestUserLocationUpdates()

        Assert.assertNotNull(mapViewModel.loadingStatus.value)
        Assert.assertTrue(mapViewModel.loadingStatus.value!!)
        verify(gpsInfoRepository).subscribe(any())
    }

    /**
     * Integration test - pass data from real [GpsInfoRepository] stream
     */
    @Test
    fun passDataFromRepositoryIntegrationTest(){
        val gpsInfoRepository = GpsInfoRepository(
                app, rxSchedulersFacade, restApiRepository, sharedPreferencesRepository)
        mapViewModel = MapViewModel(
                app, rxSchedulersFacade, gpsInfoRepository, sharedPreferencesRepository)

        mapViewModel.requestUserLocationUpdates()
        val testLocation = mock<UserLocation>()
        gpsInfoRepository.next(testLocation)

        try{ //"Temporary" fix for parallel execution due to MutableLiveData
            Thread.sleep(1)
        } catch (e: Exception){}

        Assert.assertEquals(testLocation, mapViewModel.location.value)
        Assert.assertNotNull(mapViewModel.loadingStatus.value)
        Assert.assertFalse(mapViewModel.loadingStatus.value!!)
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            SharedPreferencesRepository.locationUnits = TestUtils.randomString()
            SharedPreferencesRepository.altUnits = TestUtils.randomString()
            SharedPreferencesRepository.accUnits = TestUtils.randomString()
            SharedPreferencesRepository.speedUnits = TestUtils.randomString()
        }
    }
}