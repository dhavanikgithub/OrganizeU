package com.dk.organizeu.activity_admin

import android.R.attr.enabled
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.dk.organizeu.R
import com.dk.organizeu.databinding.ActivityAdminBinding
import com.dk.organizeu.listener.DrawerLocker
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint


class AdminActivity : AppCompatActivity(), DrawerLocker {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navHostFragment:NavHostFragment
    private lateinit var viewModel: AdminViewModel
    companion object{
        const val TAG = "OrganizeU-AdminActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_admin)
        viewModel = ViewModelProvider(this)[AdminViewModel::class.java]
        binding.apply {
            try {
                // Set the toolbar as the action bar
                setSupportActionBar(adminToolbar)

                // Find the NavHostFragment
                navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentCAA) as NavHostFragment

                // Define the navigation graph with top-level destinations
                appBarConfiguration = AppBarConfiguration.Builder(
                    setOf(
                        R.id.settingsFragmentAdmin,
                    )
                ).build()

                // Get the NavController associated with the NavHostFragment
                navController = navHostFragment.findNavController()

                // Set up the action bar with the navigation controller and configuration
                setupActionBarWithNavController(navController, appBarConfiguration)



                /*val mainMenuIcon = findViewById<ImageView>(R.id.iconMenu)
                mainMenuIcon.setOnClickListener {
                    if (adminDL.isDrawerOpen(GravityCompat.START)) {
                        adminDL.closeDrawer(GravityCompat.START)
                    } else {
                        adminDL.openDrawer(GravityCompat.START)
                    }
                }*/
            } catch (e: Exception) {
                // Log and handle any exceptions that occur
                Log.e(TAG, e.message.toString())
                this@AdminActivity.unexpectedErrorMessagePrint(e)
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                when(destination.id)
                {
                    R.id.settingsFragmentAdmin -> {
                        setDrawerEnabled(false)
                        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
                    }
                    else -> {
                        setDrawerEnabled(true)
                        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
                    }
                }
            }


            // Set a navigation item selected listener for the navigation view
            adminNV.setNavigationItemSelectedListener { menuItem ->
                // Handle different menu item clicks
                var isMenuSelect = false
                when (menuItem.itemId) {
                    R.id.nav_academic -> {
                        try {
                            // Check if the academic fragment is not already selected
                            if (!isDrawerMenuSelect(R.id.nav_academic)) {
                                // Clear the back stack up to academicFragment and navigate to the academic fragment
                                navController.popBackStack(R.id.academicFragment, false)
                                isMenuSelect = true
                            }
                        } catch (e: Exception) {
                            // Log and handle any exceptions that occur
                            Log.e(TAG, e.message.toString())
                            this@AdminActivity.unexpectedErrorMessagePrint(e)
                            isMenuSelect = false
                        }
                    }
                    R.id.nav_timetable -> {
                        try {
                            // Check if the timetable fragment is not already selected
                            if (!isDrawerMenuSelect(R.id.nav_timetable)) {
                                // Clear the back stack up to academicFragment and navigate to the timetable fragment
                                navController.popBackStack(R.id.academicFragment, false)
                                navController.navigate(R.id.timetableFragment)
                                isMenuSelect = true
                            }
                        } catch (e: Exception) {
                            // Log and handle any exceptions that occur
                            Log.e(TAG, e.message.toString())
                            this@AdminActivity.unexpectedErrorMessagePrint(e)
                            isMenuSelect = false
                        }

                    }
                    R.id.nav_faculty -> {
                        try {
                            // Check if the faculty fragment is not already selected
                            if (!isDrawerMenuSelect(R.id.nav_faculty)) {
                                // Clear the back stack up to academicFragment and navigate to the faculty fragment
                                navController.popBackStack(R.id.academicFragment, false)
                                navController.navigate(R.id.facultyFragment)
                                isMenuSelect = true
                            }
                        } catch (e: Exception) {
                            // Log and handle any exceptions that occur
                            Log.e(TAG, e.message.toString())
                            this@AdminActivity.unexpectedErrorMessagePrint(e)
                        }
                    }
                    R.id.nav_room -> {
                        try {
                            // Check if the room fragment is not already selected
                            if (!isDrawerMenuSelect(R.id.nav_room)) {
                                // Clear the back stack up to academicFragment and navigate to the room fragment
                                navController.popBackStack(R.id.academicFragment, false)
                                navController.navigate(R.id.roomsFragment)
                                isMenuSelect = true
                            }
                        } catch (e: Exception) {
                            // Log and handle any exceptions that occur
                            Log.e(TAG, e.message.toString())
                            this@AdminActivity.unexpectedErrorMessagePrint(e)
                        }
                    }
                    R.id.nav_subject -> {
                        try {
                            // Check if the subject fragment is not already selected
                            if (!isDrawerMenuSelect(R.id.nav_subject)) {
                                // Clear the back stack up to academicFragment and navigate to the subject fragment
                                navController.popBackStack(R.id.academicFragment, false)
                                navController.navigate(R.id.subjectsFragment)
                                isMenuSelect = true
                            }
                        } catch (e: Exception) {
                            // Log and handle any exceptions that occur
                            Log.e(TAG, e.message.toString())
                            this@AdminActivity.unexpectedErrorMessagePrint(e)
                        }
                    }
                    R.id.nav_aboutUs -> {
                        this@AdminActivity.showToast("!Implement Soon!")
                    }
                    R.id.nav_sign_out ->{
                        this@AdminActivity.showToast("!Implement Soon!")
                    }
                    R.id.nav_settings -> {
                        try {
                            if (!isDrawerMenuSelect(R.id.nav_settings)) {

                                navController.popBackStack(R.id.settingsFragmentAdmin, true)
                                navController.navigate(R.id.settingsFragmentAdmin)
                                isMenuSelect = true
                            }
                        } catch (e: Exception) {
                            // Log and handle any exceptions that occur
                            Log.e(TAG, e.message.toString())
                            this@AdminActivity.unexpectedErrorMessagePrint(e)
                        }
                    }

                }
                if(isMenuSelect)
                {
                    // Toggle the drawer menu after handling the click event
                    toggleDrawerMenu()
                    viewModel.selectedMenu = menuItem.itemId
                }
                // Indicate that the event has been handled successfully
                isMenuSelect
            }

        }
    }


    // Override the onOptionsItemSelected method to handle action bar item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(viewModel.selectedMenu) {
            R.id.nav_settings -> {
                viewModel.selectedMenu = 0
                super.onOptionsItemSelected(item)
            }
            else -> {
                toggleDrawerMenu()
                true
            }
        }

    }



    // Override the onSupportNavigateUp method to handle navigation up events
    override fun onSupportNavigateUp(): Boolean {
        // Navigate up in the navigation controller's navigation hierarchy
        // If the navigation controller successfully navigates up, return true
        // Otherwise, let the superclass handle the navigation up event
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }


    // Define a function to select a menu item in the drawer menu
    fun drawerMenuSelect(itemId:Int){
        binding.apply {
            try {
                // Find the menu item with the given itemId in the navigation view's menu
                val defaultMenuItem = adminNV.menu.findItem(itemId)

                // Check the found menu item to visually indicate selection
                defaultMenuItem.isChecked = true
            } catch (e: Exception) {
                // Log and handle any exceptions that occur
                Log.e(TAG, e.message.toString())
                this@AdminActivity.unexpectedErrorMessagePrint(e)
            }
        }
    }


    // Define a function to toggle the drawer menu open/close state
    fun toggleDrawerMenu() {
        binding.apply {
            // Check if the drawer menu is currently open
            if (adminDL.isDrawerOpen(GravityCompat.START)) {
                // If open, close the drawer menu
                adminDL.closeDrawer(GravityCompat.START)
            } else {
                // If closed, open the drawer menu
                adminDL.openDrawer(GravityCompat.START)
            }
        }
    }


    // Define a function to check if a menu item in the drawer menu is selected
    @Throws(Exception::class)
    fun isDrawerMenuSelect(itemId: Int): Boolean {
        binding.apply {
            try {
                // Find the menu item with the given itemId in the navigation view's menu
                val defaultMenuItem = adminNV.menu.findItem(itemId)
                // Return whether the menu item is checked (selected)
                return defaultMenuItem.isChecked
            } catch (e: Exception) {
                // Log and handle any exceptions that occur
                Log.e(TAG, e.message.toString())
                this@AdminActivity.unexpectedErrorMessagePrint(e)
                // Re-throw the exception to propagate it to the caller
                throw e
            }
        }
    }

    override fun setDrawerEnabled(enabled: Boolean) {
        val lockMode = if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        binding.adminDL.setDrawerLockMode(lockMode)
    }

}