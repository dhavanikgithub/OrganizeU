package com.dk.organizeu.activity_student

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
import com.dk.organizeu.databinding.ActivityStudentBinding
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
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {

            try {
                setSupportActionBar(toolbarStudent)
                navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerViewStudent) as NavHostFragment

                appBarConfiguration = AppBarConfiguration.Builder(
                    setOf(
                        R.id.homeFragment,
                    )
                ).build()

                navController = navHostFragment.findNavController()
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
                Log.e(TAG,e.message.toString())
                this@StudentActivity.unexpectedErrorMessagePrint(e)
            }



            navigationViewStudent.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_home -> {
                        try {
                            toggleDrawerMenu()
                            navHostFragment.findNavController().popBackStack(R.id.homeFragment,false)
                            true
                        } catch (e: Exception) {
                            Log.e(TAG,e.message.toString())
                            this@StudentActivity.unexpectedErrorMessagePrint(e)
                            false
                        }
                    }
                    R.id.nav_available_class_rooms -> {
                        toggleDrawerMenu()
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
                try {// Check if the current fragment is the home fragment

                    if (navController.currentDestination?.id == R.id.homeFragment) {
                        // Open the drawer only if the home fragment is active
                        toggleDrawerMenu()
                        return true
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.message.toString())
                    this@StudentActivity.unexpectedErrorMessagePrint(e)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggleDrawerMenu() {
        binding.apply {
            try {
                if (studentDL.isDrawerOpen(GravityCompat.START)) {
                    studentDL.closeDrawer(GravityCompat.START)
                } else {
                    studentDL.openDrawer(GravityCompat.START)
                }
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                this@StudentActivity.unexpectedErrorMessagePrint(e)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        try {
            return navController.navigateUp() || super.onSupportNavigateUp()
        } catch (e: Exception) {
            Log.e(TAG,e.message.toString())
            this@StudentActivity.unexpectedErrorMessagePrint(e)
            throw e
        }
    }

    fun drawerMenuSelect(itemId:Int){
        binding.apply {
            try {
                val defaultMenuItem = navigationViewStudent.menu.findItem(itemId)
                defaultMenuItem.isChecked = true
            } catch (e: Exception) {
                Log.e(TAG,e.message.toString())
                this@StudentActivity.unexpectedErrorMessagePrint(e)
            }
        }
    }
}