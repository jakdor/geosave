package com.jakdor.geosave

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.ui.gpsinfo.GpsInfoViewModel
import com.jakdor.geosave.utils.RxSchedulersFacade
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
        on { ui() }.thenReturn(Schedulers.computation())
    }
    private val gpsInfoRepository = mock<GpsInfoRepository>{
        on { subscribe(any()) }.thenReturn(com.nhaarman.mockito_kotlin.mock())
    }

    private var gpsInfoViewModel = GpsInfoViewModel(app, rxSchedulersFacade, gpsInfoRepository)

    /**
     * Test if copy event String forwarded to clipboardQueue stream
     */
    @Test
    fun onCopyButtonClickedTest(){
        val testStr = TestUtils.randomString()

        gpsInfoViewModel.onCopyButtonClicked(testStr)

        Assert.assertEquals(testStr, gpsInfoViewModel.clipboardQueue.value)
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
        val gpsInfoRepository = GpsInfoRepository(rxSchedulersFacade)
        gpsInfoViewModel = GpsInfoViewModel(app, rxSchedulersFacade, gpsInfoRepository)

        gpsInfoViewModel.requestUserLocationUpdates()
        val testLocation = mock<UserLocation>()
        gpsInfoRepository.next(testLocation)

        Assert.assertEquals(testLocation, gpsInfoViewModel.location.value)
        Assert.assertNotNull(gpsInfoViewModel.loadingStatus.value)
        Assert.assertFalse(gpsInfoViewModel.loadingStatus.value!!)
    }
}