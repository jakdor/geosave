package com.jakdor.geosave.ui.gpsinfo

import android.arch.lifecycle.MutableLiveData
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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

/**
 * Fragment displaying GPS info
 */
class GpsInfoFragment: Fragment(), InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var viewModel: GpsInfoViewModel? = null
    lateinit var binding: FragmentGpsInfoBinding

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

        binding.viewModel = viewModel
        viewModel?.requestUserLocationUpdates()
        observeUserLocation()
        observeClipboardCopyQueue()
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
     * Observe [GpsInfoViewModel] for updates on location [MutableLiveData] stream
     */
    fun observeUserLocation(){
        viewModel?.location?.observe(this, Observer {
            handleUserLocation(it)
        })
    }

    /**
     * Handle new [UserLocation] object
     * Pass formatted String variables instead of model to layout for better testability
     */
    fun handleUserLocation(location: UserLocation?){
        val pos = String.format(Locale.US, "%f, %f", location?.latitude, location?.longitude)
        binding.position = pos

        if(location?.altitude != 0.0){
            val alt = String.format("%.2f m", location?.altitude)
            binding.altitude = alt
            binding.provider = getString(R.string.provider_gps)
        } else {
            binding.provider = getString(R.string.provider_gsm)
        }

        val acc = String.format("%.2f m", location?.accuracy)
        binding.accuracy = acc

        val speed = String.format("%.2f m/s", location?.speed)
        binding.speed = speed

        val bearing = String.format("%.2f\u00b0", location?.bearing)
        binding.bearing = bearing
    }

    /**
     * Observe [GpsInfoViewModel] for updates on clipboardCopyQueue [MutableLiveData] stream
     */
    fun observeClipboardCopyQueue(){
        viewModel?.clipboardCopyQueue?.observe(this, Observer {
           handleClipboardCopy(it)
        })
    }

    /**
     * Copy to clipboard string received from [GpsInfoViewModel] after user click on copy button
     */
    fun handleClipboardCopy(text: String?){
        val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(getString(R.string.clipboard_label), text)
        clipboard.primaryClip = clip

        Toast.makeText(activity, getString(R.string.clipboard_toast), Toast.LENGTH_SHORT).show()
    }

    companion object {

        const val CLASS_TAG = "GpsInfoFragment"

        fun newInstance(): GpsInfoFragment{
            val args = Bundle()
            val fragment = GpsInfoFragment()
            fragment.arguments = args
            fragment.retainInstance = true
            return fragment
        }
    }
}