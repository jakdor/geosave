package com.jakdor.geosave

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.common.repository.SharedPreferencesRepository
import com.jakdor.geosave.ui.map.MapViewModel
import com.jakdor.geosave.utils.RxSchedulersFacade
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class MapViewModelTest {
    
    @get:Rule
    private val thrownRule = ExpectedException.none()
    
    @get:Rule
    var instantExecRule = InstantTaskExecutorRule()
    
    private val app = mock<Application>()
    private val rxSchedulersFacade = mock<RxSchedulersFacade> { 
        on { computation() }.thenReturn(Schedulers.computation())
    }
    private val gpsInfoRepository = mock<GpsInfoRepository> {
        on { subscribe(any()) }.thenReturn(com.nhaarman.mockito_kotlin.mock())
    }
    private val sharedPreferencesRepository = mock<SharedPreferencesRepository>()

    private var mapViewModel = MapViewModel(
            app, rxSchedulersFacade, gpsInfoRepository, sharedPreferencesRepository)

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
        val gpsInfoRepository = GpsInfoRepository(rxSchedulersFacade)
        mapViewModel = MapViewModel(
                app, rxSchedulersFacade, gpsInfoRepository, sharedPreferencesRepository)

        mapViewModel.requestUserLocationUpdates()
        val testLocation = mock<UserLocation>()
        gpsInfoRepository.next(testLocation)

        Assert.assertEquals(testLocation, mapViewModel.location.value)
        Assert.assertNotNull(mapViewModel.loadingStatus.value)
        Assert.assertFalse(mapViewModel.loadingStatus.value!!)
    }
}