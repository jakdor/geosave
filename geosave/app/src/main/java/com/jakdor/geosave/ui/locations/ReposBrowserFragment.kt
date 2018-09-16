/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.locations

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakdor.geosave.R
import com.jakdor.geosave.di.InjectableFragment
import kotlinx.android.synthetic.main.fragment_repos_browser.*
import javax.inject.Inject

/**
 * Fragment for browsing user repositories
 */
class ReposBrowserFragment: Fragment(), InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var viewModel: ReposBrowserViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_repos_browser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repos_fab_new.setOnClickListener { viewModel?.onFabCreateNewClicked() }
        repos_fab_public.setOnClickListener { viewModel?.onFabBrowsePublicClicked() }
        repos_fab_private.setOnClickListener { viewModel?.onFabJoinPrivateClicked() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(viewModel == null){
            viewModel = ViewModelProviders.of(this, viewModelFactory)
                    .get(ReposBrowserViewModel::class.java)
        }
    }

    companion object {

        const val CLASS_TAG = "ReposBrowserFragment"

        fun newInstance(): ReposBrowserFragment{
            val args = Bundle()
            val fragment = ReposBrowserFragment()
            fragment.arguments = args
            fragment.retainInstance = true
            return fragment
        }
    }
}