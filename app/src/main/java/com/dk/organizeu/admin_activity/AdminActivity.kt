package com.dk.organizeu.admin_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.dk.organizeu.R
import com.dk.organizeu.admin_activity.fragments.academic.AcademicFragment
import com.dk.organizeu.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navHostFragment:NavHostFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            setSupportActionBar(adminToolbar)
            navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentCAA) as NavHostFragment

            appBarConfiguration = AppBarConfiguration.Builder(
                setOf(
                    R.id.academicFragment,
                )
            ).build()

            navController = navHostFragment.findNavController()
            val appBarConfiguration = AppBarConfiguration(navController.graph)
            setupActionBarWithNavController(navController, appBarConfiguration)



            /*val mainMenuIcon = findViewById<ImageView>(R.id.iconMenu)
            mainMenuIcon.setOnClickListener {
                if (adminDL.isDrawerOpen(GravityCompat.START)) {
                    adminDL.closeDrawer(GravityCompat.START)
                } else {
                    adminDL.openDrawer(GravityCompat.START)
                }
            }*/

            adminNV.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_academic -> {
                        if(!isDrawerMenuSelect(R.id.nav_academic))
                        {
                            toggleDrawerMenu()
                            navController.popBackStack(R.id.academicFragment,true)
                            navController.navigate(R.id.academicFragment)
                            //mainMenuIcon.callOnClick()
                            true
                        }
                        else{
                            toggleDrawerMenu()
                            navController.popBackStack(R.id.academicFragment,true)
                            //mainMenuIcon.callOnClick()
                            false
                        }

                    }
                    R.id.nav_timetable -> {
                        if(!isDrawerMenuSelect(R.id.nav_timetable))
                        {
                            toggleDrawerMenu()
                            navController.popBackStack(R.id.timetableFragment,true)
                            navController.navigate(R.id.timetableFragment)
                            //mainMenuIcon.callOnClick()
                            true
                        }
                        else{
                            toggleDrawerMenu()
                            navController.popBackStack(R.id.timetableFragment,true)
                            //mainMenuIcon.callOnClick()
                            false
                        }
                    }
                    R.id.nav_faculty -> {
                        if(!isDrawerMenuSelect(R.id.nav_faculty))
                        {
                            toggleDrawerMenu()
                            navController.popBackStack(R.id.facultyFragment,true)
                            navController.navigate(R.id.facultyFragment)
                            //mainMenuIcon.callOnClick()
                            true
                        }
                        else{
                            toggleDrawerMenu()
                            navController.popBackStack(R.id.facultyFragment,true)
                            //mainMenuIcon.callOnClick()
                            false
                        }
                    }
                    R.id.nav_room -> {
                        if(!isDrawerMenuSelect(R.id.nav_room))
                        {
                            toggleDrawerMenu()
                            navController.popBackStack(R.id.roomsFragment,true)
                            navController.navigate(R.id.roomsFragment)
                            //mainMenuIcon.callOnClick()
                            true
                        }
                        else{
                            toggleDrawerMenu()
                            navController.popBackStack(R.id.roomsFragment,true)
                            //mainMenuIcon.callOnClick()
                            false
                        }
                    }
                    R.id.nav_subject -> {
                        if(!isDrawerMenuSelect(R.id.nav_subject))
                        {
                            toggleDrawerMenu()
                            navController.popBackStack(R.id.subjectsFragment,false)
                            navController.navigate(R.id.subjectsFragment)
                            //mainMenuIcon.callOnClick()

                            true
                        }
                        else{
                            toggleDrawerMenu()
                            navController.popBackStack(R.id.subjectsFragment,false)
                            //mainMenuIcon.callOnClick()
                            false
                        }
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

                if (navController.currentDestination?.id == R.id.academicFragment) {
                    // Open the drawer only if the home fragment is active
                    toggleDrawerMenu()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun drawerMenuSelect(itemId:Int){
        binding.apply {
            val defaultMenuItem = adminNV.menu.findItem(itemId)
            defaultMenuItem.isChecked = true
        }
    }

    fun toggleDrawerMenu()
    {
        binding.apply {
            if (adminDL.isDrawerOpen(GravityCompat.START)) {
                adminDL.closeDrawer(GravityCompat.START)
            } else {
                adminDL.openDrawer(GravityCompat.START)
            }
        }
    }

    fun isDrawerMenuSelect(itemId:Int):Boolean{
        binding.apply {
            val defaultMenuItem = adminNV.menu.findItem(itemId)
           return defaultMenuItem.isChecked
        }
    }
}