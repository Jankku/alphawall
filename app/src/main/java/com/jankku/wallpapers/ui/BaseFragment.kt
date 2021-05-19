package com.jankku.wallpapers.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.paging.ExperimentalPagingApi

/**
 * All fragments extend this BaseFragment
 * Sets the bottom nav visibility with bottomNavigationVisibility variable
 * https://stackoverflow.com/a/62552740
 */
@ExperimentalPagingApi
abstract class BaseFragment : Fragment() {

    protected open var bottomNavigationVisibility = View.VISIBLE
    private lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // get the reference of the parent activity and call the setBottomNavigationVisibility method.
        if (activity is MainActivity) {
            mainActivity = activity as MainActivity
            mainActivity.setBottomNavigationVisibility(bottomNavigationVisibility)
        }
    }

    override fun onResume() {
        super.onResume()
        if (activity is MainActivity) {
            mainActivity.setBottomNavigationVisibility(bottomNavigationVisibility)
        }
    }
}