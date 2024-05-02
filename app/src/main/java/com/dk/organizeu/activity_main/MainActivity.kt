package com.dk.organizeu.activity_main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.dk.organizeu.R
import com.dk.organizeu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        // Find the NavHostFragment
        navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerViewMain) as NavHostFragment
        // Get the NavController associated with the NavHostFragment
        navController = navHostFragment.findNavController()
    }
    // Override the onSupportNavigateUp method to handle navigation up events
    override fun onSupportNavigateUp(): Boolean {
        // Navigate up in the navigation controller's navigation hierarchy
        // If the navigation controller successfully navigates up, return true
        // Otherwise, let the superclass handle the navigation up event
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}