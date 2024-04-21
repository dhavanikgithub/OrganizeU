package com.dk.organizeu.activity_main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dk.organizeu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {

        }
    }
}