package com.jakdor.geosave.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.BottomNavigationView
import com.jakdor.geosave.R
import com.jakdor.geosave.ui.gpsinfo.GpsInfoFragment
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import com.crashlytics.android.Crashlytics
import com.jakdor.geosave.service.gps.GpsService
import io.fabric.sdk.android.Fabric
import timber.log.Timber

class MainActivity : DaggerAppCompatActivity(), MainContract.MainView, ServiceConnection {

    @Inject
    lateinit var presenter: MainPresenter

    private var serviceBound: Boolean = false

    private val mOnNavigationItemSelectedListener
            = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_gps_info -> {
                presenter.onGpsInfoTabClicked()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_map -> {
                presenter.onMapTabClicked()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_locations -> {
                presenter.onLocationsTabClicked()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics()) //todo move to splash
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        serviceBound = bindService(Intent(this, GpsService::class.java),
                this, Context.BIND_AUTO_CREATE)

        presenter.start()
    }

    override fun onStop() {
        super.onStop()
        if(serviceBound){
            unbindService(this)
        }
    }

    /**
     * Create or reattach [GpsInfoFragment]
     */
    override fun switchToGpsInfoFragment() {
        if (supportFragmentManager.findFragmentByTag(GpsInfoFragment.CLASS_TAG) == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.mainFragmentLayout, //todo check add vs replace
                            GpsInfoFragment.newInstance(), GpsInfoFragment.CLASS_TAG)
                    .commit()
            Timber.i("Created %s", GpsInfoFragment.CLASS_TAG)
        } else {
            supportFragmentManager
                    .beginTransaction()
                    .attach(supportFragmentManager.findFragmentByTag(GpsInfoFragment.CLASS_TAG))
                    .commit()
            Timber.i("Reattached %s", GpsInfoFragment.CLASS_TAG)
        }
    }

    override fun switchToMapFragment() {
    }

    override fun switchToLocationsFragment() {
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        presenter.attachService((p1 as GpsService.LocalBinder).service)
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        presenter.detachService()
    }
}
