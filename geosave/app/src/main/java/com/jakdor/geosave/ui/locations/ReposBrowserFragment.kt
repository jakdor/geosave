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
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.di.InjectableFragment
import com.jakdor.geosave.ui.adapters.RepositoryAdapter
import com.jakdor.geosave.ui.elements.AddRepoDialog
import kotlinx.android.synthetic.main.fragment_repos_browser.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Fragment for browsing user repositories
 */
class ReposBrowserFragment: Fragment(), InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var viewModel: ReposBrowserViewModel? = null
    private var recyclerViewInit = false
    private lateinit var repositoryAdapter: RepositoryAdapter
    private lateinit var addRepoDialog: AddRepoDialog

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

        observeDialogLunchRequest()
        observeLoadingStatus()
        observeDismissDialogRequest()
        observeDialogLoadingStatus()
        observeReposList()
        observeToast()
        viewModel?.loadUserRepos()
    }

    /**
     * Dismiss dialogs during screen rotation
     */
    override fun onPause() {
        super.onPause()
        viewModel?.dismissDialog(DialogRequest.ALL)
        if(::addRepoDialog.isInitialized && addRepoDialog.isShowing) addRepoDialog.dismiss()
    }

    /**
     * Fix for [RepositoryAdapter] memory leak
     */
    override fun onDestroyView() {
        super.onDestroyView()
        repos_recycler_view.adapter = null
    }

    /**
     * Observe [ReposBrowserViewModel] dialogLunchRequest
     */
    fun observeDialogLunchRequest(){
        viewModel?.dialogLunchRequest?.observe(this, Observer {
            handleDialogLunchRequest(it)
        })
    }

    /**
     * Handle dialog lunch code
     */
    fun handleDialogLunchRequest(code: DialogRequest?){
        if(code != null){
            repos_fab_menu.collapse()
            when(code){
                ReposBrowserFragment.DialogRequest.CREATE_NEW -> lunchAddRepoDialog()
                ReposBrowserFragment.DialogRequest.BROWSE_PUBLIC -> {}
                ReposBrowserFragment.DialogRequest.JOIN_PRIVATE -> {}
                else -> {}
            }
        }
    }

    /**
     * Lunch [AddRepoDialog]
     */
    fun lunchAddRepoDialog(){
        if(context != null) {
            addRepoDialog = AddRepoDialog(context!!)
            addRepoDialog.cancelButtonOnClickListener = View.OnClickListener {
                viewModel?.onAddRepoDialogCancelClicked()
            }
            addRepoDialog.createButtonOnClickListener = View.OnClickListener {
                val repo = addRepoDialog.createNewRepoObj()
                if(repo != null) viewModel?.createNewRepo(repo)
            }
            addRepoDialog.show()
            Timber.i("lunched AddRepoDialog")
        } else {
            Timber.e("unable to lunch AddRepoDialog, context is null")
        }
    }

    /**
     * Observe [ReposBrowserViewModel] loadingStatus
     */
    fun observeLoadingStatus(){
        viewModel?.loadingStatus?.observe(this, Observer { handleLoadingStatus(it) })
    }

    /**
     * Observe [ReposBrowserViewModel] dialogLoadingStatus
     */
    fun observeDialogLoadingStatus(){
        viewModel?.dialogLoadingStatus?.observe(this, Observer {
            handleNewDailogLoadinStatus(it)
        })
    }

    fun handleNewDailogLoadinStatus(status: Boolean?) {
        if(status != null){
            if(::addRepoDialog.isInitialized){
                addRepoDialog.handleNewDialogLoadingStatus(status)
            }
        }
    }

    /**
     * Observe [ReposBrowserViewModel] dialogDismissDialog
     */
    fun observeDismissDialogRequest(){
        viewModel?.dialogDismissRequest?.observe(this, Observer {
            handleNewDismissDialogRequestValue(it)
        })
    }

    /**
     * Handle new dismissDialogRequest value
     */
    fun handleNewDismissDialogRequestValue(dialogCode: DialogRequest?){
        if(dialogCode != null){
            if(dialogCode == DialogRequest.CREATE_NEW || dialogCode == DialogRequest.ALL)
                addRepoDialog.dismiss()
        }
    }

    /**
     * Handle loadingStatus
     */
    fun handleLoadingStatus(status: Boolean?){
        if(status != null)
            when(status){
                true -> {
                    repos_swipe_refresh.isRefreshing = true
                    repos_no_repo_message.visibility = View.GONE
                    repos_recycler_view.visibility = View.VISIBLE
                }
                false -> repos_swipe_refresh.isRefreshing = false
            }
    }

    /**
     * Observe [ReposBrowserViewModel] reposList
     */
    fun observeReposList(){
        viewModel?.reposList?.observe(this, Observer { handleReposList(it) })
    }

    /**
     * Handle new reposList update
     */
    fun handleReposList(repos: MutableList<Repo?>?){
        if(repos != null && !recyclerViewInit) loadRecyclerView(repos)
        else if(repos != null && recyclerViewInit) updateRecyclerView(repos)
    }

    /**
     * Observe [ReposBrowserViewModel] toast
     */
    fun observeToast(){
        viewModel?.toast?.observe(this, Observer { displayToast(it) })
    }

    /**
     * Display toast message
     */
    fun displayToast(msg: String?){
        if(msg != null) Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    /**
     * Load RecyclerView with user repositories
     * @param repoList loaded by [ReposBrowserViewModel]
     */
    fun loadRecyclerView(repoList: MutableList<Repo?>){
        if(repoList.isEmpty()){
            repos_no_repo_message.visibility = View.VISIBLE
            repos_recycler_view.visibility = View.GONE
            return
        } else {
            repos_no_repo_message.visibility = View.GONE
            repos_recycler_view.visibility = View.VISIBLE
        }

        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        repos_recycler_view.layoutManager = linearLayoutManager
        repositoryAdapter = RepositoryAdapter(Vector(repoList), viewModel, getHeight(),
                activity?.resources?.configuration?.orientation, getScreenRatio())
        repos_recycler_view.adapter = repositoryAdapter
        recyclerViewInit = true
    }

    /**
     * update RecyclerView with new user repositories
     * @param repoList loaded by [ReposBrowserViewModel]
     */
    fun updateRecyclerView(repoList: MutableList<Repo?>){
        repos_no_repo_message.visibility = View.GONE
        repos_recycler_view.visibility = View.VISIBLE
        repositoryAdapter.updateReposList(repoList)
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
        NONE, CREATE_NEW, BROWSE_PUBLIC, JOIN_PRIVATE, ALL
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