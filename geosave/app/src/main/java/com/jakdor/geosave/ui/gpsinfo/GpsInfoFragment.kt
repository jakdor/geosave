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
import javax.inject.Inject
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.jakdor.geosave.common.repository.LocationConverter
import com.jakdor.geosave.common.repository.SharedPreferencesRepository

/**
 * Fragment displaying GPS info
 */
class GpsInfoFragment: Fragment(), InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var viewModel: GpsInfoViewModel? = null
    lateinit var binding: FragmentGpsInfoBinding

    private lateinit var preferencesMap: Map<String, Int>

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
        observePreferencesMap()
    }

    override fun onResume() {
        super.onResume()
        viewModel?.requestPreferencesUpdate()
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
            if(it != null) handleUserLocation(it)
        })
    }

    /**
     * Handle new [UserLocation] object
     * Pass formatted String variables instead of model to layout for better testability
     */
    fun handleUserLocation(location: UserLocation){

        //location
        when(preferencesMap[SharedPreferencesRepository.locationUnits]){
            0 -> { //decimal
                binding.position =
                        LocationConverter.decimalFormat(location.latitude, location.longitude)
            }
            1 -> { //sexigesimal
                binding.position = LocationConverter.dmsFormat(location.latitude, location.longitude)
            }
            2 -> { //decimal degrees
                binding.position =
                        LocationConverter.decimalDegreesFormat(location.latitude, location.longitude)
            }
            3 -> { //degrees decimal minutes
                binding.position =
                        LocationConverter.dmFormat(location.latitude, location.longitude)
            }
        }

        if(location.altitude != -999.0){
            val alt = String.format("%.2f m", location.altitude)
            binding.altitude = alt
            binding.provider = getString(R.string.provider_gps)
        } else {
            binding.provider = getString(R.string.provider_gsm)
        }

        val acc = String.format("%.2f m", location.accuracy)
        binding.accuracy = acc

        val speed = String.format("%.2f m/s", location.speed)
        binding.speed = speed

        val bearing = String.format("%.2f\u00b0", location.bearing)
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
     * Set null value to clipboardCopyQueue [MutableLiveData] to prevent re-handling after
     * screen rotation
     */
    fun handleClipboardCopy(text: String?){
        if(text != null) {
            val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(getString(R.string.clipboard_label), text)
            clipboard.primaryClip = clip

            Toast.makeText(activity, getString(R.string.clipboard_toast), Toast.LENGTH_SHORT).show()

            viewModel?.clipboardCopyQueue?.value = null
        }
    }

    /**
     * Observe [GpsInfoViewModel] preferences [MutableLiveData] stream for updates on user preferences
     */
    fun observePreferencesMap(){
        viewModel?.preferences?.observe(this, Observer {
            handlePreferencesMap(it)
        })
    }

    /**
     * Store preferences update values locally
     */
    fun handlePreferencesMap(map: MutableMap<String, Int>?){
        if(map != null){
            this.preferencesMap = map
        }
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