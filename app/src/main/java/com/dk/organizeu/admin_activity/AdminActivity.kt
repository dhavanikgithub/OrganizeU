package com.dk.organizeu.admin_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
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
            setupActionBarWithNavController(navController, appBarConfiguration)



            /*val mainMenuIcon = findViewById<ImageView>(R.id.iconMenu)
            mainMenuIcon.setOnClickListener {
                if (adminDL.isDrawerOpen(GravityCompat.START)) {
                    adminDL.closeDrawer(GravityCompat.START)
                } else {
                    adminDL.openDrawer(GravityCompat.START)
                }
            }*/


            navController.addOnDestinationChangedListener { _, destination, _ ->
                val isHomeFragment = destination.id == R.id.academicFragment
                if (isHomeFragment) {
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
                } else {
                    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
                }
            }

            adminNV.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_academic -> {
                        if(!isDrawerMenuSelect(R.id.nav_academic))
                        {
                            navController.popBackStack(R.id.academicFragment,false)
                            navController.navigate(R.id.academicFragment)
                        }

                    }
                    R.id.nav_timetable -> {
                        if(!isDrawerMenuSelect(R.id.nav_timetable))
                        {
                            navController.popBackStack(R.id.academicFragment,false)
                            navController.navigate(R.id.timetableFragment)
                        }
                    }
                    R.id.nav_faculty -> {
                        if(!isDrawerMenuSelect(R.id.nav_faculty))
                        {
                            navController.popBackStack(R.id.academicFragment,false)
                            navController.navigate(R.id.facultyFragment)
                        }
                    }
                    R.id.nav_room -> {
                        if(!isDrawerMenuSelect(R.id.nav_room))
                        {
                            navController.popBackStack(R.id.academicFragment,false)
                            navController.navigate(R.id.roomsFragment)
                        }
                    }
                    R.id.nav_subject -> {
                        if(!isDrawerMenuSelect(R.id.nav_subject))
                        {
                            navController.popBackStack(R.id.academicFragment,false)
                            navController.navigate(R.id.subjectsFragment)
                        }
                    }
                }
                toggleDrawerMenu()
                true
            }
        }
    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (navController.currentDestination?.id == R.id.academicFragment) {
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