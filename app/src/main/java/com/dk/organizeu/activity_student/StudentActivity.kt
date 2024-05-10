package com.dk.organizeu.activity_student

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
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
import com.dk.organizeu.databinding.ActivityStudentBinding
import com.dk.organizeu.enum_class.StudentLocalDBKey
import com.dk.organizeu.listener.DrawerLocker
import com.dk.organizeu.utils.SharedPreferencesManager
import com.dk.organizeu.utils.UtilFunction
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint

class StudentActivity : AppCompatActivity(), DrawerLocker {
    private lateinit var binding: ActivityStudentBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navHostFragment:NavHostFragment
    private lateinit var viewModel: StudentViewModel
    private lateinit var navHeader: View
    private lateinit var studentNameTextView: TextView
    private lateinit var  studentOtherDetailsTextView: TextView

    companion object{
        const val TAG = "OrganizeU-StudentActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_student)
        viewModel = ViewModelProvider(this)[StudentViewModel::class.java]
        navHeader = binding.navigationViewStudent.getHeaderView(0)
        binding.apply {
            studentNameTextView = navHeader.findViewById(R.id.txtStudentName)
            studentOtherDetailsTextView = navHeader.findViewById(R.id.txtStudentOtherDetails)

            studentNameTextView.text = SharedPreferencesManager.getString(this@StudentActivity,StudentLocalDBKey.NAME.displayName)
            val studentOtherDetails = "Sem: ${SharedPreferencesManager.getString(this@StudentActivity,StudentLocalDBKey.SEMESTER.displayName)} | ${SharedPreferencesManager.getString(this@StudentActivity,StudentLocalDBKey.CLASS.displayName)} (${SharedPreferencesManager.getString(this@StudentActivity,StudentLocalDBKey.BATCH.displayName)})"
            studentOtherDetailsTextView.text = studentOtherDetails

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

                // Add a destination changed listener to the NavController
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    when(destination.id)
                    {
                        R.id.settingsFragmentStudent -> {
                            setDrawerEnabled(false)
                            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
                        }
                        else -> {
                            setDrawerEnabled(true)
                            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
                        }
                    }
                }
            } catch (e: Exception) {
                // Log and handle any exceptions that occur during setup
                Log.e(TAG, e.message.toString())
                this@StudentActivity.unexpectedErrorMessagePrint(e)
            }




            // Set up the navigation item selected listener for the student navigation drawer
            navigationViewStudent.setNavigationItemSelectedListener { menuItem ->
                var isMenuSelect = false
                when (menuItem.itemId) {

                    // Handle selection of the "Home" menu item
                    R.id.nav_home -> {
                        isMenuSelect = try {
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
                    R.id.nav_available_class_rooms -> {
                        navHostFragment.findNavController().popBackStack(R.id.homeFragment,false)
                        navHostFragment.findNavController().navigate(R.id.availableClassRoomFragment)
                        isMenuSelect = true
                    }
                    R.id.nav_aboutUs -> {
                        UtilFunction.underConstructionDialog(this@StudentActivity)
                        isMenuSelect = false
                    }
                    R.id.nav_settings -> {
                        toggleDrawerMenu()
                        val bundle = Bundle()
                        bundle.putBoolean("isStudent",true)
                        navHostFragment.findNavController().popBackStack(R.id.settingsFragmentStudent, true)
                        navHostFragment.findNavController().navigate(R.id.settingsFragmentStudent,bundle)
                        isMenuSelect = true
                    }
                    else -> isMenuSelect = false
                }
                if(isMenuSelect)
                {
                    viewModel.selectedMenu = menuItem.itemId
                    toggleDrawerMenu()
                }
                isMenuSelect
            }

        }
    }
    // Override the onOptionsItemSelected method to handle options menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (viewModel.selectedMenu) {
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
            return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
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

    override fun setDrawerEnabled(enabled: Boolean) {
        val lockMode = if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        binding.studentDL.setDrawerLockMode(lockMode)
    }


}