package com.dk.organizeu.activity_student

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.dk.organizeu.R
import com.dk.organizeu.databinding.ActivityStudentBinding
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint

class StudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navHostFragment:NavHostFragment

    companion object{
        const val TAG = "OrganizeU-StudentActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_student)
        binding.apply {

            try {
                // Set up the toolbar and navigation for the student activity
                setSupportActionBar(toolbarStudent)
                navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerViewStudent) as NavHostFragment

                // Set up the app bar configuration with the home fragment
                appBarConfiguration = AppBarConfiguration.Builder(
                    setOf(
                        R.id.homeFragment,
                    )
                ).build()

                // Get the NavController for navigating between destinations
                navController = navHostFragment.findNavController()

                // Set up the action bar with the NavController and app bar configuration
                setupActionBarWithNavController(navController, appBarConfiguration)

                /*val mainMenuIcon = findViewById<ImageView>(R.id.iconMenu)
                    mainMenuIcon.setOnClickListener {
                        if (studentDL.isDrawerOpen(GravityCompat.START)) {
                            studentDL.closeDrawer(GravityCompat.START)
                        } else {
                            studentDL.openDrawer(GravityCompat.START)
                        }
                    }*/
            } catch (e: Exception) {
                // Log and handle any exceptions that occur during setup
                Log.e(TAG, e.message.toString())
                this@StudentActivity.unexpectedErrorMessagePrint(e)
            }




            // Set up the navigation item selected listener for the student navigation drawer
            navigationViewStudent.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    // Handle selection of the "Home" menu item
                    R.id.nav_home -> {
                        try {
                            // Toggle the drawer menu and navigate to the "Home" fragment, popping the back stack if necessary
                            toggleDrawerMenu()
                            navHostFragment.findNavController().popBackStack(R.id.homeFragment, false)
                            true // Return true to indicate that the item selection has been handled
                        } catch (e: Exception) {
                            // Log and handle any exceptions that occur
                            Log.e(TAG, e.message.toString())
                            this@StudentActivity.unexpectedErrorMessagePrint(e)
                            false // Return false to indicate that the item selection has not been handled
                        }
                    }
                    // Handle selection of the "Available Class Rooms" menu item
                    R.id.nav_available_class_rooms -> {
                        this@StudentActivity.showToast("!Implement Soon!")
                        false // Return false to indicate that the item selection has not been handled
                    }
                    R.id.nav_aboutUs -> {
                        this@StudentActivity.showToast("!Implement Soon!")
                        false
                    }
                    R.id.nav_signOut -> {
                        this@StudentActivity.showToast("!Implement Soon!")
                        false
                    }
                    R.id.nav_settings -> {
                        this@StudentActivity.showToast("!Implement Soon!")
                        false
                    }
                    else -> false // Return false for any other menu items
                }
            }

        }
    }
    // Override the onOptionsItemSelected method to handle options menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                try {
                    // Check if the selected menu item is the home button
                    if (navController.currentDestination?.id == R.id.homeFragment) {
                        // If the current fragment is the home fragment, toggle the drawer menu
                        toggleDrawerMenu()
                        return true // Return true to indicate that the item selection has been handled
                    }
                } catch (e: Exception) {
                    // Log and handle any exceptions that occur
                    Log.e(TAG, e.message.toString())
                    this@StudentActivity.unexpectedErrorMessagePrint(e)
                }
            }
        }
        // Call the superclass method to handle the selected menu item
        return super.onOptionsItemSelected(item)
    }


    /**
     * Toggles the visibility of the drawer menu.
     */
    private fun toggleDrawerMenu() {
        binding.apply {
            try {
                // Check if the drawer menu is currently open
                if (studentDL.isDrawerOpen(GravityCompat.START)) {
                    // If open, close the drawer menu
                    studentDL.closeDrawer(GravityCompat.START)
                } else {
                    // If closed, open the drawer menu
                    studentDL.openDrawer(GravityCompat.START)
                }
            } catch (e: Exception) {
                // Log and handle any exceptions that occur
                Log.e(TAG, e.message.toString())
                this@StudentActivity.unexpectedErrorMessagePrint(e)
            }
        }
    }


    /**
     * Handles the Up navigation action.
     * This method is called when the user presses the Up button in the action bar.
     * It navigates up in the application's navigation hierarchy.
     *
     * @return True if navigation was successful, false otherwise.
     */
    override fun onSupportNavigateUp(): Boolean {
        try {
            // Attempt to navigate up in the navigation hierarchy
            return navController.navigateUp() || super.onSupportNavigateUp()
        } catch (e: Exception) {
            // Log and handle any exceptions that occur
            Log.e(TAG, e.message.toString())
            this@StudentActivity.unexpectedErrorMessagePrint(e)
            // Propagate the exception
            throw e
        }
    }


    /**
     * Selects the specified item in the student navigation drawer menu.
     *
     * @param itemId The ID of the menu item to select.
     */
    fun drawerMenuSelect(itemId: Int) {
        binding.apply {
            try {
                // Find the specified menu item in the student navigation drawer menu
                val defaultMenuItem = navigationViewStudent.menu.findItem(itemId)
                // Set the specified menu item as checked
                defaultMenuItem.isChecked = true
            } catch (e: Exception) {
                // Log and handle any exceptions that occur
                Log.e(TAG, e.message.toString())
                this@StudentActivity.unexpectedErrorMessagePrint(e)
            }
        }
    }


}