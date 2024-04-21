package com.dk.organizeu.activity_admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
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
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                this@AdminActivity.unexpectedErrorMessagePrint(e)
            }


            navController.addOnDestinationChangedListener { _, destination, _ ->
                try {
                    val isHomeFragment = destination.id == R.id.academicFragment
                    if (isHomeFragment) {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
                    } else {
                        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    this@AdminActivity.unexpectedErrorMessagePrint(e)
                }
            }

            adminNV.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_academic -> {
                        try {
                            if(!isDrawerMenuSelect(R.id.nav_academic))
                            {
                                navController.popBackStack(R.id.academicFragment,false)
                                navController.navigate(R.id.academicFragment)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG,e.message.toString())
                            this@AdminActivity.unexpectedErrorMessagePrint(e)
                        }

                    }
                    R.id.nav_timetable -> {
                        try {
                            if(!isDrawerMenuSelect(R.id.nav_timetable))
                            {
                                navController.popBackStack(R.id.academicFragment,false)
                                navController.navigate(R.id.timetableFragment)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG,e.message.toString())
                            this@AdminActivity.unexpectedErrorMessagePrint(e)
                        }
                    }
                    R.id.nav_faculty -> {
                        try {
                            if(!isDrawerMenuSelect(R.id.nav_faculty))
                            {
                                navController.popBackStack(R.id.academicFragment,false)
                                navController.navigate(R.id.facultyFragment)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG,e.message.toString())
                            this@AdminActivity.unexpectedErrorMessagePrint(e)
                        }
                    }
                    R.id.nav_room -> {
                        try {
                            if(!isDrawerMenuSelect(R.id.nav_room))
                            {
                                navController.popBackStack(R.id.academicFragment,false)
                                navController.navigate(R.id.roomsFragment)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG,e.message.toString())
                            this@AdminActivity.unexpectedErrorMessagePrint(e)
                        }
                    }
                    R.id.nav_subject -> {
                        try {
                            if(!isDrawerMenuSelect(R.id.nav_subject))
                            {
                                navController.popBackStack(R.id.academicFragment,false)
                                navController.navigate(R.id.subjectsFragment)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG,e.message.toString())
                            this@AdminActivity.unexpectedErrorMessagePrint(e)
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
            try {
                val defaultMenuItem = adminNV.menu.findItem(itemId)
                defaultMenuItem.isChecked = true
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                this@AdminActivity.unexpectedErrorMessagePrint(e)
            }
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

    @Throws(Exception::class)
    fun isDrawerMenuSelect(itemId:Int):Boolean{
        binding.apply {
            try {
                val defaultMenuItem = adminNV.menu.findItem(itemId)
                return defaultMenuItem.isChecked
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                this@AdminActivity.unexpectedErrorMessagePrint(e)
                throw e
            }
        }
    }
}