package com.jakdor.geosave.ui.main

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import com.jakdor.geosave.R
import com.jakdor.geosave.ui.gpsinfo.GpsInfoFragment
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import timber.log.Timber


class MainActivity : DaggerAppCompatActivity(), MainContract.MainView {

    @Inject
    lateinit var presenter: MainPresenter

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

        presenter.start()
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
}
