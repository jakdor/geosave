package com.jakdor.geosave

import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.ui.main.MainActivity
import com.jakdor.geosave.ui.main.MainPresenter
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.junit.MockitoJUnit

class MainPresenterTest {

    @get:Rule
    var thrown = ExpectedException.none()

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()

    private val view: MainActivity = mock {

    }

    private val gpsInfoRepository: GpsInfoRepository = mock {  }

    private val mainPresenter: MainPresenter = MainPresenter(view, gpsInfoRepository)

    /**
     * Test start() - lunching correct fragment
     */
    @Test
    fun startTest(){
        mainPresenter.start()

        verify(view).switchToGpsInfoFragment()
    }

    /**
     * Test pause() - stop location updates (Google api location provider)
     */
    @Test
    fun pauseGMSActiveTest(){
        //locationUpdates = true
        mainPresenter.gmsLocationUpdatesActive()

        mainPresenter.pause()

        verify(view).stopLocationUpdates()
        verify(view, never()).fallbackStopLocationUpdates()
    }

    /**
     * Test pause() (Google api location provider)
     */
    @Test
    fun pauseGMSNotActiveTest(){
        //locationUpdates = false by default
        mainPresenter.pause()

        verify(view, never()).stopLocationUpdates()
        verify(view, never()).fallbackStopLocationUpdates()
    }

    /**
     * Test pause() - stop location updates (Native location provider)
     */
    @Test
    fun pauseNativeActiveTest(){
        //fallbackLocationUpdates = true
        mainPresenter.fallbackLocationUpdatesActive()

        mainPresenter.pause()

        verify(view).fallbackStopLocationUpdates()
        verify(view, never()).stopLocationUpdates()
    }

    /**
     * Test pause() (Native location provider)
     */
    @Test
    fun pauseNativeNotActiveTest(){
        //fallbackLocationUpdates = false by default
        mainPresenter.pause()

        verify(view, never()).fallbackStopLocationUpdates()
        verify(view, never()).stopLocationUpdates()
    }

    /**
     * Test resume() - start location updates (Google api location provider)
     */
    @Test
    fun resumeGMSActiveTest(){
        mainPresenter.gmsLocationUpdatesActive()

        mainPresenter.resume()

        verify(view).gmsSetupLocationUpdates()
        verify(view, never()).fallbackStartLocationUpdates()
    }

    /**
     * Test resume() (Google api location provider)
     */
    @Test
    fun resumeGMSNotActiveTest(){
        mainPresenter.resume()

        verify(view, never()).gmsSetupLocationUpdates()
        verify(view, never()).fallbackStartLocationUpdates()
    }

    /**
     * Test resume() - start location updates (Native location provider)
     */
    @Test
    fun resumeNativeActiveTest(){
        mainPresenter.fallbackLocationUpdatesActive()

        mainPresenter.resume()

        verify(view).fallbackLocationManagerSetup()
        verify(view).fallbackCheckGps()
        verify(view).fallbackStartLocationUpdates()
        verify(view, never()).gmsSetupLocationUpdates()
    }

    /**
     * Test resume() (Native location provider)
     */
    @Test
    fun resumeNativeNotActiveTest(){
        mainPresenter.resume()

        verify(view, never()).fallbackLocationManagerSetup()
        verify(view, never()).fallbackCheckGps()
        verify(view, never()).fallbackStartLocationUpdates()
        verify(view, never()).gmsSetupLocationUpdates()
    }
}