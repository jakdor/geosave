package com.jakdor.geosave

import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.ui.main.MainActivity
import com.jakdor.geosave.ui.main.MainPresenter
import com.nhaarman.mockito_kotlin.mock
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

    private var view: MainActivity = mock {

    }

    private val gpsInfoRepository: GpsInfoRepository = mock {  }

    private val mainPresenter: MainPresenter = MainPresenter(view, gpsInfoRepository)

    @Test
    fun startTest(){
        mainPresenter.start()
        verify(view).switchToGpsInfoFragment()
    }
}