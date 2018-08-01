package com.jakdor.geosave.arch

import java.io.Serializable

/**
 * MVP presenter base class
 */
abstract class BasePresenter<View> protected constructor(protected var view: View?): Serializable {

    /**
     * attach view
     */
    open fun bindView(view: View?){
        this.view = view
    }

    /**
     * detach view
     */
    open fun unbindView(){
        view = null
    }

    /**
     * Check if view attached
     */
    fun isBinded(): Boolean{
        return view != null
    }

    /**
     * Start state forwarded from view layer
     */
    open fun start() {}

    /**
     * Pause state forwarded from view layer
     */
    open fun pause() {}

    /**
     * Resume state forwarded from view layer
     */
    open fun resume() {}

    /**
     * Stop state forwarded from view layer
     */
    open fun stop() {}

    /**
     * Destroy state forwarded from view layer
     */
    open fun destroy() {}

}