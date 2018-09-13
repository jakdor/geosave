/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.adapters

import android.databinding.BindingAdapter
import android.support.constraint.ConstraintLayout
import android.support.constraint.Guideline

class GuidelineBindingAdapters {
    companion object {

        /**
         * Binding adapter for dynamic guidelines percentage
         */
        @JvmStatic
        @BindingAdapter("layout_constraintGuide_percent")
        fun setLayoutConstraintGuidePercent(guideline: Guideline, percent: Float) {
            val params = guideline.layoutParams as ConstraintLayout.LayoutParams
            params.guidePercent = percent
            guideline.layoutParams = params
        }
    }
}