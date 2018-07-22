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