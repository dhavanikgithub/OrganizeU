package com.dk.organizeu.activity_admin

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.dk.organizeu.R
import com.dk.organizeu.databinding.ActivityAdminBinding
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navHostFragment:NavHostFragment
    companion object{
        const val TAG = "OrganizeU-AdminActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            try {
                // Set the toolbar as the action bar
                setSupportActionBar(adminToolbar)

                // Find the NavHostFragment
                navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentCAA) as NavHostFragment

                // Define the navigation graph with top-level destinations
                appBarConfiguration = AppBarConfiguration.Builder(
                    setOf(
                        R.id.academicFragment,
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



            // Add a destination changed listener to the NavController
            navController.addOnDestinationChangedListener { _, destination, _ ->
                try {
                    // Check if the current destination is the home fragment (academicFragment)
                    val isHomeFragment = destination.id == R.id.academicFragment

                    // If it's the home fragment, display the home/up button with the menu icon
                    if (isHomeFragment) {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
                    } else {
                        // If it's not the home fragment, display the back arrow icon
                        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
                    }
                } catch (e: Exception) {
                    // Log and handle any exceptions that occur
                    Log.e(TAG, e.message.toString())
                    this@AdminActivity.unexpectedErrorMessagePrint(e)
                }
            }


            // Set a navigation item selected listener for the navigation view
            adminNV.setNavigationItemSelectedListener { menuItem ->
                // Handle different menu item clicks
                when (menuItem.itemId) {
                    R.id.nav_academic -> {
                        try {
                            // Check if the academic fragment is not already selected
                            if (!isDrawerMenuSelect(R.id.nav_academic)) {
                                // Clear the back stack up to academicFragment and navigate to the academic fragment
                                navController.popBackStack(R.id.academicFragment, false)
                                navController.navigate(R.id.academicFragment)
                            }
                        } catch (e: Exception) {
                            // Log and handle any exceptions that occur
                            Log.e(TAG, e.message.toString())
                            this@AdminActivity.unexpectedErrorMessagePrint(e)
                        }
                    }
                    R.id.nav_timetable -> {
                        try {
                            // Check if the timetable fragment is not already selected
                            if (!isDrawerMenuSelect(R.id.nav_timetable)) {
                                // Clear the back stack up to academicFragment and navigate to the timetable fragment
                                navController.popBackStack(R.id.academicFragment, false)
                                navController.navigate(R.id.timetableFragment)
                            }
                        } catch (e: Exception) {
                            // Log and handle any exceptions that occur
                            Log.e(TAG, e.message.toString())
                            this@AdminActivity.unexpectedErrorMessagePrint(e)
                        }
                    }
                    R.id.nav_faculty -> {
                        try {
                            // Check if the faculty fragment is not already selected
                            if (!isDrawerMenuSelect(R.id.nav_faculty)) {
                                // Clear the back stack up to academicFragment and navigate to the faculty fragment
                                navController.popBackStack(R.id.academicFragment, false)
                                navController.navigate(R.id.facultyFragment)
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
                            }
                        } catch (e: Exception) {
                            // Log and handle any exceptions that occur
                            Log.e(TAG, e.message.toString())
                            this@AdminActivity.unexpectedErrorMessagePrint(e)
                        }
                    }
                }
                // Toggle the drawer menu after handling the click event
                toggleDrawerMenu()
                // Indicate that the event has been handled successfully
                true
            }

        }
    }




    // Override the onOptionsItemSelected method to handle action bar item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Check if the clicked item is the home/up button
        when (item.itemId) {
            android.R.id.home -> {
                // Check if the current destination is the academic fragment
                if (navController.currentDestination?.id == R.id.academicFragment) {
                    // Toggle the drawer menu when the home/up button is clicked in the academic fragment
                    toggleDrawerMenu()
                    // Return true to indicate that the action has been handled
                    return true
                }
            }
        }
        // If the clicked item is not the home/up button or the current destination is not the academic fragment,
        // let the superclass handle the click event
        return super.onOptionsItemSelected(item)
    }



    // Override the onSupportNavigateUp method to handle navigation up events
    override fun onSupportNavigateUp(): Boolean {
        // Navigate up in the navigation controller's navigation hierarchy
        // If the navigation controller successfully navigates up, return true
        // Otherwise, let the superclass handle the navigation up event
        return navController.navigateUp() || super.onSupportNavigateUp()
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

}