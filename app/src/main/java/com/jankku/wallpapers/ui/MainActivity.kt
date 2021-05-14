package com.jankku.wallpapers.ui

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.aghajari.zoomhelper.ZoomHelper
import com.jankku.wallpapers.R
import com.jankku.wallpapers.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
        binding.bottomNavigation.setOnNavigationItemReselectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    // Nothing to prevent API calls
                }
            }
        }

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.categoryFragment, R.id.settingsFragment)
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        supportActionBar!!.elevation = 0f
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.nav_host_fragment).navigateUp()

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return ZoomHelper
            .getInstance()
            .dispatchTouchEvent(ev!!, this) || super.dispatchTouchEvent(ev)
    }

    fun setBottomNavigationVisibility(visibility: Int) {
        // get the reference of the bottomNavigation and set the visibility.
        binding.bottomNavigation.visibility = visibility
    }
}