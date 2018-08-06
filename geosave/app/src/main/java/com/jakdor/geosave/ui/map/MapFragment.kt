package com.jakdor.geosave.ui.map

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakdor.geosave.R
import com.jakdor.geosave.di.InjectableFragment
import javax.inject.Inject

class MapFragment: Fragment(), InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var viewModel: MapViewModel? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map_overlay, container, false)
    }

    companion object: InjectableFragment {
        const val CLASS_TAG = "MapFragment"

        fun newInstance(): MapFragment{
            val args = Bundle()
            val fragment = MapFragment()
            fragment.arguments = args
            fragment.retainInstance = true
            return fragment
        }
    }
}