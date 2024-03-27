package com.dk.organizeu.student_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
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
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentCSA) as NavHostFragment

            appBarConfiguration = AppBarConfiguration.Builder(
                setOf(
                    R.id.homeFragment,
                )
            ).build()

            navController = navHostFragment.findNavController()

            val mainMenuIcon = findViewById<ImageView>(R.id.menuIV)
            mainMenuIcon.setOnClickListener {
                if (studentDL.isDrawerOpen(GravityCompat.START)) {
                    studentDL.closeDrawer(GravityCompat.START)
                } else {
                    studentDL.openDrawer(GravityCompat.START)
                }
            }



            studentNV.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_home -> {
                        navHostFragment.findNavController().popBackStack(R.id.homeFragment,false)
                        mainMenuIcon.callOnClick()
                        true
                    }
                    R.id.nav_available_class_rooms -> {
//                        navController.popBackStack(R.id.availableClassRoomFragment,false)
//                        navHostFragment.findNavController().navigate(R.id.availableClassRoomFragment)
                        mainMenuIcon.callOnClick()
                        true
                    }
                    else -> false
                }
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun drawerMenuSelect(itemId:Int){
        binding.apply {
            val defaultMenuItem = studentNV.menu.findItem(itemId)
            defaultMenuItem.isChecked = true
        }
    }
}