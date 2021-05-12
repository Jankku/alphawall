package com.jankku.wallpapers.ui

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.aghajari.zoomhelper.ZoomHelper
import com.jankku.wallpapers.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

        setupActionBarWithNavController(navHostFragment.findNavController())
        supportActionBar!!.elevation = 0f
        ZoomHelper.getInstance().layoutTheme = android.R.style.Theme_Translucent_NoTitleBar_Fullscreen
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.nav_host_fragment).navigateUp()

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return ZoomHelper
            .getInstance()
            .dispatchTouchEvent(ev!!, this) || super.dispatchTouchEvent(ev)
    }
}