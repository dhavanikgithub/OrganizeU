package com.dk.organizeu.student_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.dk.organizeu.R
import com.dk.organizeu.databinding.ActivityStudentBinding

class StudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            setSupportActionBar(toolbarStudent)
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerViewStudent) as NavHostFragment

            appBarConfiguration = AppBarConfiguration.Builder(
                setOf(
                    R.id.homeFragment,
                )
            ).build()

            navController = navHostFragment.findNavController()
            val appBarConfiguration = AppBarConfiguration(navController.graph)
            setupActionBarWithNavController(navController, appBarConfiguration)

            /*val mainMenuIcon = findViewById<ImageView>(R.id.iconMenu)
            mainMenuIcon.setOnClickListener {
                if (studentDL.isDrawerOpen(GravityCompat.START)) {
                    studentDL.closeDrawer(GravityCompat.START)
                } else {
                    studentDL.openDrawer(GravityCompat.START)
                }
            }*/



            navigationViewStudent.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_home -> {
                        toggleDrawerMenu()
                        navHostFragment.findNavController().popBackStack(R.id.homeFragment,false)
                        //mainMenuIcon.callOnClick()
                        true
                    }
                    R.id.nav_available_class_rooms -> {
                        toggleDrawerMenu()
//                        navController.popBackStack(R.id.availableClassRoomFragment,false)
//                        navHostFragment.findNavController().navigate(R.id.availableClassRoomFragment)
                        //mainMenuIcon.callOnClick()
                        false
                    }
                    else -> false
                }
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Check if the current fragment is the home fragment

                if (navController.currentDestination?.id == R.id.homeFragment) {
                    // Open the drawer only if the home fragment is active
                    toggleDrawerMenu()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggleDrawerMenu() {
        binding.apply {
            if (studentDL.isDrawerOpen(GravityCompat.START)) {
                studentDL.closeDrawer(GravityCompat.START)
            } else {
                studentDL.openDrawer(GravityCompat.START)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun drawerMenuSelect(itemId:Int){
        binding.apply {
            val defaultMenuItem = navigationViewStudent.menu.findItem(itemId)
            defaultMenuItem.isChecked = true
        }
    }
}