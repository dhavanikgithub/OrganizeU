package com.dk.organizeu.admin_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.dk.organizeu.R
import com.dk.organizeu.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentCAA) as NavHostFragment

            appBarConfiguration = AppBarConfiguration.Builder(
                setOf(
                    R.id.academicFragment,
                )
            ).build()

            navController = navHostFragment.findNavController()

            val mainMenuIcon = findViewById<ImageView>(R.id.menuIV)
            mainMenuIcon.setOnClickListener {
                if (adminDL.isDrawerOpen(GravityCompat.START)) {
                    adminDL.closeDrawer(GravityCompat.START)
                } else {
                    adminDL.openDrawer(GravityCompat.START)
                }
            }

            adminNV.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_academic -> {
//                        navHostFragment.findNavController().popBackStack(R.id.academicFragment,false)
//                        navHostFragment.findNavController().navigate(R.id.academicFragment)
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
}