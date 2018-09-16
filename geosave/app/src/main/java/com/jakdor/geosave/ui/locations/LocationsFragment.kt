/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.locations

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
import timber.log.Timber
import javax.inject.Inject

class LocationsFragment: Fragment(), InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var viewModel: LocationsViewModel? = null

    private val fragmentMap: MutableMap<String, Fragment> = mutableMapOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_locations, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(viewModel == null){
            viewModel = ViewModelProviders.of(this, viewModelFactory)
                    .get(LocationsViewModel::class.java)
        }

        viewModel?.requestUpdatesOnCurrentFragment()
        observeCurrentFragmentId()
    }

    /**
     * Observe [LocationsViewModel] updates on current child fragment id
     */
    fun observeCurrentFragmentId(){
        viewModel?.currentFragmentId?.observe(this, Observer {
            handleNewCurrentFragmentId(it)
        })
    }

    /**
     * Handle new current fragment id received from [LocationsViewModel] currentFragmentId
     */
    fun handleNewCurrentFragmentId(id: String?){
        if(id != null){
            when(id){
                ReposBrowserFragment.CLASS_TAG -> switchToReposBrowserFragement()
            }
        }
    }

    /**
     * Create or reattach [ReposBrowserFragment]
     */
    fun switchToReposBrowserFragement(){
        if (!fragmentMap.containsKey(ReposBrowserFragment.CLASS_TAG)) {
            fragmentMap[ReposBrowserFragment.CLASS_TAG] = ReposBrowserFragment.newInstance()
            Timber.i("Created %s", ReposBrowserFragment.CLASS_TAG)
        }

        childFragmentManager
                .beginTransaction()
                .replace(R.id.locations_fragment_layout,
                        fragmentMap[ReposBrowserFragment.CLASS_TAG], ReposBrowserFragment.CLASS_TAG)
                .commit()

        Timber.i("Attached child fragment: %s", ReposBrowserFragment.CLASS_TAG)
    }

    companion object {

        const val CLASS_TAG = "LocationsFragment"

        fun newInstance(): LocationsFragment{
            val args = Bundle()
            val fragment = LocationsFragment()
            fragment.arguments = args
            fragment.retainInstance = true
            return fragment
        }
    }
}