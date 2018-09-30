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
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
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
    private var loadedPics = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_repo, container, false)

        binding.repoToolbar.setNavigationOnClickListener{ viewModel?.returnFromRepoFragment() }
        binding.repoToolbarEdit.visibility = View.GONE

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
        observeContributorsPicUrl()
        viewModel?.observeChosenRepository()
    }

    fun observeRepo(){
        viewModel?.repoIsOwnerPair?.observe(this, Observer { handleNewRepoIsOwnerPair(it) })
    }

    /**
     * Update view with new [Repo] object and info about ownership
     */
    fun handleNewRepoIsOwnerPair(repoIsOwnerPair: Pair<Repo?, Boolean>?){
        if(repoIsOwnerPair?.first != null){
            val repo = repoIsOwnerPair.first
            binding.repo = repo

            binding.repoContributorsIcon1.visibility = View.GONE
            binding.repoContributorsIcon2.visibility = View.GONE
            binding.repoContributorsIcon3.visibility = View.GONE
            loadedPics = 0
            viewModel?.requestContributorsPicUrls(repo)

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
            } else {
                binding.repoToolbarEdit.visibility = View.VISIBLE
            }
        }
    }

    fun observeContributorsPicUrl(){
        viewModel?.repoContributorPicUrl?.observe(this, Observer {
            handleContributorPicUrl(it)
        })
    }

    /**
     * Load received contributors pic urls onto ImageViews
     */
    fun handleContributorPicUrl(url: String?){
        if(url != null){
            val imageView = when(loadedPics){
                0 -> binding.repoContributorsIcon1
                1 -> binding.repoContributorsIcon2
                2 -> binding.repoContributorsIcon3
                else -> binding.repoContributorsIcon1
            }

            GlideApp.with(binding.root)
                    .load(url)
                    .apply(RequestOptions()
                            .placeholder(R.drawable.repo_icon_placeholder)
                            .error(R.drawable.repo_icon_placeholder)
                            .centerCrop()
                            .circleCrop())
                    .listener(object : RequestListener<Drawable>{
                        override fun onLoadFailed(e: GlideException?, model: Any?,
                                                  target: Target<Drawable>?,
                                                  isFirstResource: Boolean): Boolean {
                            imageView.visibility = View.VISIBLE
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?,
                                                     target: Target<Drawable>?,
                                                     dataSource: DataSource?,
                                                     isFirstResource: Boolean): Boolean {
                            imageView.visibility = View.VISIBLE
                            return false
                        }
                    })
                    .into(imageView)

            ++loadedPics
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