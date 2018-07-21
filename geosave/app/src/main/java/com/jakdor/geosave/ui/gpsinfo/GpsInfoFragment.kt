package com.jakdor.geosave.ui.gpsinfo

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakdor.geosave.R
import com.jakdor.geosave.di.InjectableFragment
import com.jakdor.geosave.common.model.UserLocation
import timber.log.Timber
import javax.inject.Inject

/**
 * Fragment displaying GPS info
 */
class GpsInfoFragment: Fragment(), InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var viewModel: GpsInfoViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gps_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(viewModel == null){
            viewModel = ViewModelProviders.of(this, viewModelFactory)
                    .get(GpsInfoViewModel::class.java)
        }

        viewModel?.requestUserLocationUpdates()
        observeUserLocation()
    }

    /**
     * Handle new [UserLocation] object
     */
    fun observeUserLocation(){
        viewModel?.userLocation?.observe(this, Observer {
            t -> Timber.i("MVVM hurray, %s", t.toString())
        })
    }

    companion object {

        const val CLASS_TAG = "GpsInfoFragment"

        fun newInstance(): GpsInfoFragment{
            val args = Bundle()
            val fragment = GpsInfoFragment()
            fragment.arguments = args
            return fragment
        }
    }
}