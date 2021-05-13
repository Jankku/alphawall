package com.jankku.wallpapers.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

// https://stackoverflow.com/a/62552740
abstract class BaseFragment : Fragment() {

    protected open var bottomNavigationVisibility = View.VISIBLE
    private lateinit var mainActivity: MainActivity

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