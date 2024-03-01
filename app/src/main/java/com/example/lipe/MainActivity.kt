package com.example.lipe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.replace
import com.example.lipe.databinding.ActivityMainBinding
import com.google.android.libraries.places.widget.AutocompleteSupportFragment

class MainActivity : AppCompatActivity() {

    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}