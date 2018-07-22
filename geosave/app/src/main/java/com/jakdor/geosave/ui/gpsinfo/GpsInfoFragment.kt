package com.jakdor.geosave.ui.gpsinfo

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakdor.geosave.R
import com.jakdor.geosave.di.InjectableFragment
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.databinding.FragmentGpsInfoBinding
import java.util.*
import javax.inject.Inject

/**
 * Fragment displaying GPS info
 */
class GpsInfoFragment: Fragment(), InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var viewModel: GpsInfoViewModel? = null
    private lateinit var binding: FragmentGpsInfoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_gps_info, container, false)
        layoutInit()
        return binding.root
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
     * Set initial value for fields (can't be set in xml for some reason)
     */
    fun layoutInit(){
        binding.position = getString(R.string.value_unknown)
        binding.altitude = getString(R.string.value_unknown)
        binding.accuracy = getString(R.string.value_unknown)
        binding.speed = getString(R.string.value_unknown)
        binding.bearing = getString(R.string.value_unknown)
        binding.provider = getString(R.string.value_unknown)
    }

    /**
     * Handle new [UserLocation] object
     * Pass formatted String variables instead of model to layout for better testability
     */
    fun observeUserLocation(){
        viewModel?.userLocation?.observe(this, Observer {
            val pos = String.format(Locale.US, "%f, %f", it?.latitude, it?.longitude)
            binding.position = pos

            if(it?.altitude != 0.0){
                val alt = String.format("%.2f m", it?.altitude)
                binding.altitude = alt
                binding.provider = getString(R.string.provider_gps)
            } else {
                binding.provider = getString(R.string.provider_gsm)
            }

            val acc = String.format("%.2f m", it?.accuracy)
            binding.accuracy = acc

            val speed = String.format("%.2f m/s", it?.speed)
            binding.speed = speed

            val bearing = String.format("%.2f\u00b0", it?.bearing)
            binding.bearing = bearing
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