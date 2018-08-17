package com.jakdor.geosave

import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.ui.main.MainActivity
import com.jakdor.geosave.ui.main.MainPresenter
import com.nhaarman.mockito_kotlin.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.InOrder
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

    /**
     * Correct tab switching for GpsInfo tab
     */
    @Test
    fun onGpsInfoTabClickedTest(){
        mainPresenter.onGpsInfoTabClicked()
        verify(view, times(1)).switchToGpsInfoFragment()

        mainPresenter.onGpsInfoTabClicked()
        verify(view, times(1)).switchToGpsInfoFragment()
    }

    /**
     * Correct tab switching for Map tab
     */
    @Test
    fun onMapTabClickedTest(){
        mainPresenter.onMapTabClicked()
        verify(view, times(1)).switchToMapFragment()

        mainPresenter.onMapTabClicked()
        verify(view, times(1)).switchToMapFragment()
    }

    /**
     * Correct tab switching for Location tab
     */
    @Test
    fun onLocationTabClickedTest(){
        mainPresenter.onLocationsTabClicked()
        verify(view, times(1)).switchToLocationsFragment()

        mainPresenter.onLocationsTabClicked()
        verify(view, times(1)).switchToLocationsFragment()
    }

    /**
     * Test presenter response to Google Api/GMS connection established
     */
    @Test
    fun gmsConnectedTest(){
        mainPresenter.gmsConnected()

        verify(view).checkPermissions()
    }

    /**
     * Test presenter response to Google Api/GMS connection failed
     */
    @Test
    fun gmsFailedTest(){
        mainPresenter.gmsFailed()

        verify(view).checkPermissions()
    }

    /**
     * permissionsGranted() status = false
     */
    @Test
    fun permissionsGrantedFalseTest(){
        mainPresenter.permissionsGranted(false)

        verify(view).displayToast(R.string.gps_permissions_declined)
    }

    /**
     * permissionsGranted() status = true, GoogleApi/GMS
     */
    @Test
    fun permissionsGrantedTrueGMSTest(){
        mainPresenter.permissionsGranted(true)

        verify(view).gmsSetupLocationUpdates()
    }

    /**
     * permissionsGranted() status = true, fallback/native
     */
    @Test
    fun permissionsGrantedTrueNativeTest(){
        mainPresenter.gmsFailed()
        mainPresenter.permissionsGranted(true)

        verify(view).fallbackLocationManagerSetup()
        verify(view).fallbackCheckGps()
        verify(view).fallbackStartLocationUpdates()
    }

    /**
     * GMS gps enable dialog, result = true
     */
    @Test
    fun gmsGpsEnableDialogTrueTest(){
        mainPresenter.gmsGpsEnableDialog(true)

        verify(view).gmsSetupLocationUpdates()
    }

    /**
     * GMS gps enable dialog, result = false
     */
    @Test
    fun gmsGpsEnableDialogFalseTest(){
        mainPresenter.gmsGpsEnableDialog(false)

        verify(view).displayToast(R.string.gps_enable_declined)
    }

    /**
     * Check correct execution order of setting up fallback/native location provider
     */
    @Test
    fun fallbackStartupTest(){
        mainPresenter.fallbackStartup()

        val inOrder: InOrder = inOrder(view)
        inOrder.verify(view).fallbackLocationManagerSetup()
        inOrder.verify(view).fallbackCheckGps()
        inOrder.verify(view).fallbackStartLocationUpdates()
    }

    /**
     * Check correct execution order of GMS GPS auto-enable fail handling
     */
    @Test
    fun fallbackGpsAutoEnableFailedTest(){
        mainPresenter.fallbackGpsAutoEnableFailed()

        val inOrder: InOrder = inOrder(view)
        inOrder.verify(view).fallbackLocationManagerSetup()
        inOrder.verify(view).fallbackCheckGps()
    }

    /**
     * fallbackGpsDialogUserResponse(), response = true
     */
    @Test
    fun fallbackGpsDialogUserResponseTrueTest(){
        mainPresenter.fallbackGpsDialogUserResponse(true)

        verify(view).fallbackTurnGpsIntent()
    }

    /**
     * fallbackGpsDialogUserResponse(), response = false
     */
    @Test
    fun fallbackGpsDialogUserResponseFalseTest(){
        mainPresenter.fallbackGpsDialogUserResponse(false)

        verify(view).displayToast(R.string.gps_fallback_dialog_no_toast)
    }
}