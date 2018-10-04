/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.common.repository.RestApiRepository
import com.jakdor.geosave.common.repository.SharedPreferencesRepository
import com.jakdor.geosave.ui.gpsinfo.GpsInfoViewModel
import com.jakdor.geosave.utils.RxSchedulersFacade
import com.jakdor.geosave.utils.TestUtils
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class GpsInfoViewModelTest{

    @get:Rule
    private val thrown = ExpectedException.none()

    @get:Rule
    var rule = InstantTaskExecutorRule()

    private val app = mock<Application>()
    private val rxSchedulersFacade = mock<RxSchedulersFacade>{
        on { computation() }.thenReturn(Schedulers.computation())
    }
    private val gpsInfoRepository = mock<GpsInfoRepository>{
        on { subscribe(any()) }.thenReturn(com.nhaarman.mockito_kotlin.mock())
    }
    private val restApiRepository = mock<RestApiRepository>()
    private val sharedPreferencesRepository = mock<SharedPreferencesRepository>()

    private var gpsInfoViewModel = GpsInfoViewModel(
            app, rxSchedulersFacade, gpsInfoRepository, sharedPreferencesRepository)

    /**
     * Test if copy event String forwarded to clipboardCopyQueue stream
     */
    @Test
    fun onCopyButtonClickedTest(){
        val testStr = TestUtils.randomString()

        gpsInfoViewModel.onCopyButtonClicked(testStr)

        Assert.assertEquals(testStr, gpsInfoViewModel.clipboardCopyQueue.value)
    }

    /**
     * Test subscribe to [GpsInfoRepository] stream
     */
    @Test
    fun requestUserLocationUpdatesTest(){
        gpsInfoViewModel.requestUserLocationUpdates()

        Assert.assertNotNull(gpsInfoViewModel.loadingStatus.value)
        Assert.assertTrue(gpsInfoViewModel.loadingStatus.value!!)
        verify(gpsInfoRepository).subscribe(any())
    }

    /**
     * Integration test - pass data from real [GpsInfoRepository] stream
     */
    @Test
    fun passDataFromRepositoryIntegrationTest(){
        val gpsInfoRepository = GpsInfoRepository(
                app, rxSchedulersFacade, restApiRepository, sharedPreferencesRepository)
        gpsInfoViewModel = GpsInfoViewModel(
                app, rxSchedulersFacade, gpsInfoRepository, sharedPreferencesRepository)

        gpsInfoViewModel.requestUserLocationUpdates()
        val testLocation = mock<UserLocation>()
        gpsInfoRepository.next(testLocation)

        try{ //"Temporary" fix for parallel execution due to MutableLiveData
            Thread.sleep(1)
        } catch (e: Exception){}

        Assert.assertEquals(testLocation, gpsInfoViewModel.location.value)
        Assert.assertNotNull(gpsInfoViewModel.loadingStatus.value)
        Assert.assertFalse(gpsInfoViewModel.loadingStatus.value!!)
    }
}