/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.locations

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.firebase.Location
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.common.repository.SharedPreferencesRepository
import com.jakdor.geosave.databinding.FragmentRepoBinding
import com.jakdor.geosave.di.InjectableFragment
import com.jakdor.geosave.ui.adapters.LocationAdapter
import com.jakdor.geosave.ui.elements.AddImageDialog
import com.jakdor.geosave.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_repo.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class RepoFragment: Fragment(), InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferencesRepository: SharedPreferencesRepository

    var viewModel: RepoViewModel? = null
    lateinit var binding: FragmentRepoBinding
    private var loadedPics = 0
    private var recyclerViewInit = false
    private lateinit var locationAdapter: LocationAdapter
    private var repoMainPicUrl = ""

    private lateinit var addImageDialog: AddImageDialog

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
        observeDialogLunchRequest()
        observeDialogDismissRequest()
        observeDialogLoadingStatus()
        viewModel?.observeContributorsPicUrls()
        viewModel?.observeChosenRepository()
    }

    /**
     * Fix for [LocationAdapter] memory leak
     */
    override fun onDestroyView() {
        super.onDestroyView()
        repo_locations_recycler_view.adapter = null
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

            repoMainPicUrl = repo?.picUrl ?: ""

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

            loadRecyclerView(repo?.locationsList)
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

            if(url.isNotEmpty() && url != "null") {
                GlideApp.with(binding.root)
                        .load(url)
                        .apply(RequestOptions()
                                .placeholder(R.drawable.repo_icon_placeholder)
                                .error(R.drawable.repo_icon_placeholder)
                                .centerCrop()
                                .circleCrop())
                        .listener(object : RequestListener<Drawable> {
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
            } else {
                GlideApp.with(binding.root)
                        .load(R.drawable.repo_icon_placeholder)
                        .apply(RequestOptions()
                                .centerCrop()
                                .circleCrop())
                        .into(imageView)
                imageView.visibility = View.VISIBLE
            }

            ++loadedPics

            Timber.i("Loaded %d contributor pic", loadedPics)
        }
    }

    fun observeDialogLunchRequest(){
        viewModel?.dialogLunchRequest?.observe(this, Observer { handleDialogLunchRequest(it) })
    }

    /**
     * Lunch dialog after receiving lunch request from viewModel
     */
    fun handleDialogLunchRequest(request: DialogRequest?){
        if(request != null){
           when(request){
               DialogRequest.ADD_IMAGE -> lunchAddImageDialog()
               DialogRequest.ALL -> {}
               DialogRequest.NONE -> {}
           }
        }
    }

    fun observeDialogDismissRequest(){
        viewModel?.dialogDismissRequest?.observe(this, Observer {
            handleDialogDismissRequest(it)
        })
    }

    /**
     * Dismiss dialog/s
     */
    fun handleDialogDismissRequest(dialogRequest: DialogRequest?){
        if(dialogRequest != null){
            when(dialogRequest){
                DialogRequest.ADD_IMAGE -> {
                    if(::addImageDialog.isInitialized) addImageDialog.dismiss()
                }
                DialogRequest.ALL -> {
                    if(::addImageDialog.isInitialized) addImageDialog.dismiss()
                }
                DialogRequest.NONE -> {}
            }
        }
    }

    fun observeDialogLoadingStatus(){
        viewModel?.dialogLoadingStatus?.observe(this, Observer {
            handleNewDailogLoadinStatus(it)
        })
    }

    fun handleNewDailogLoadinStatus(status: Boolean?) {
        if(status != null){
            if(::addImageDialog.isInitialized){
                addImageDialog.handleNewDialogLoadingStatus(status)
            }
        }
    }

    /**
     * Load RecyclerView with user repositories
     * @param locationList loaded by [ReposBrowserViewModel]
     */
    fun loadRecyclerView(locationList: MutableList<Location>?){
        if(locationList == null){
            repo_no_locations_message_frame.visibility = View.VISIBLE
            repo_locations_recycler_view.visibility = View.GONE
            return
        }

        if(locationList.isEmpty()){
            repo_no_locations_message_frame.visibility = View.VISIBLE
            repo_locations_recycler_view.visibility = View.GONE
            return
        } else {
            repo_no_locations_message_frame.visibility = View.GONE
            repo_locations_recycler_view.visibility = View.VISIBLE
        }

        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        repo_locations_recycler_view.layoutManager = linearLayoutManager
        locationAdapter = LocationAdapter(Vector(locationList), viewModel,
                sharedPreferencesRepository, getHeight(),
                activity?.resources?.configuration?.orientation, getScreenRatio())
        repo_locations_recycler_view.adapter = locationAdapter
        recyclerViewInit = true
    }

    /**
     * //todo repository location add observable
     * update RecyclerView with new user repositories
     * @param locationList loaded by [ReposBrowserViewModel]
     */
    fun updateRecyclerView(locationList: MutableList<Location?>){
        repo_no_locations_message_frame.visibility = View.GONE
        repo_locations_recycler_view.visibility = View.VISIBLE
        locationAdapter.updateReposList(locationList)
    }

    /**
     * Lunch [AddImageDialog]
     */
    fun lunchAddImageDialog(){
        if(context != null) {
            addImageDialog = AddImageDialog(context!!)
            addImageDialog.cancelButtonOnClickListener = View.OnClickListener {
                viewModel?.dismissDialog(DialogRequest.ADD_IMAGE)
            }
            addImageDialog.uploadButtonOnClickListener = View.OnClickListener {
                viewModel?.uploadPic()
            }
            addImageDialog.previewPicUrl = repoMainPicUrl
            addImageDialog.show()
            Timber.i("lunched addImageDialog")
        } else {
            Timber.e("unable to lunch addImageDialog, context is null")
        }
    }

    /**
     * Get window height for auto scaling in RecyclerView
     * @return int window height or null if unable to get height
     */
    private fun getHeight(): Int? {
        var height: Int? = null

        if (activity != null) {
            val displayMetrics = DisplayMetrics()
            activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
            height = displayMetrics.heightPixels
        }

        return height
    }

    /**
     * Get device screen ratio for accurate height auto scaling in RecyclerView after rotation
     */
    private fun getScreenRatio(): Float? {
        var screenRatio: Float? = null

        if(activity != null) {
            screenRatio = activity!!.resources.displayMetrics.widthPixels.toFloat() /
                    activity!!.resources.displayMetrics.heightPixels.toFloat()
            if(screenRatio < 1.0f){
                screenRatio = 1.0f / screenRatio
            }
        }

        return screenRatio
    }

    enum class DialogRequest{
        NONE, ADD_IMAGE, ALL
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