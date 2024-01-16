package com.example.lipe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.lipe.databinding.ActivityMainBinding
import com.google.android.libraries.places.widget.AutocompleteSupportFragment

class MainActivity : AppCompatActivity() {

    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        replaceFragment(StartFragment())
    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.all, fragment)
        fragmentTransaction.commit()
    }
}