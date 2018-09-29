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
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.request.RequestOptions
import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.databinding.FragmentRepoBinding
import com.jakdor.geosave.di.InjectableFragment
import com.jakdor.geosave.utils.GlideApp
import javax.inject.Inject

class RepoFragment: Fragment(), InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var viewModel: RepoViewModel? = null
    lateinit var binding: FragmentRepoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_repo, container, false)

        binding.repoToolbar.setNavigationOnClickListener{ viewModel?.returnFromRepoFragment() }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(viewModel == null){
            viewModel = ViewModelProviders.of(this, viewModelFactory)
                    .get(RepoViewModel::class.java)
        }
        binding.viewModel = viewModel

        observeRepo()
        viewModel?.observeChosenRepository()
    }

    fun observeRepo(){
        viewModel?.repoIsOwnerPair?.observe(this, Observer { handleNewRepoIsOwnerPair(it) })
    }

    fun handleNewRepoIsOwnerPair(repoIsOwnerPair: Pair<Repo?, Boolean>?){
        if(repoIsOwnerPair?.first != null){
            val repo = repoIsOwnerPair.first
            binding.repo = repo

            GlideApp.with(binding.root)
                    .load(repo?.picUrl)
                    .apply(RequestOptions()
                            .placeholder(R.drawable.repo_icon_placeholder_empty)
                            .centerCrop()
                            .circleCrop())
                    .into(binding.repoIcon)

            //lock owner options for non-owner user
            if(!repoIsOwnerPair.second){
                binding.repoToolbarEdit.visibility = View.GONE
            }
        }
    }

    companion object {

        const val CLASS_TAG = "RepoFragment"

        fun newInstance(): RepoFragment{
            val args = Bundle()
            val fragment = RepoFragment()
            fragment.arguments = args
            fragment.retainInstance = true
            return fragment
        }
    }
}