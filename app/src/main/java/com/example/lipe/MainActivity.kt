package com.example.lipe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.replace
import com.example.lipe.databinding.ActivityMainBinding
import com.example.lipe.friend_requests.FriendRequestsFragment
import com.example.lipe.rating_board.RatingFragment
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.material.color.DynamicColors
import com.google.android.material.internal.EdgeToEdgeUtils

class MainActivity : AppCompatActivity() {

    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(2000)
        installSplashScreen()
        setContentView(R.layout.activity_main) 
        enableEdgeToEdge()
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("INFOG", "123")
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val fragmentToLoad = intent.getStringExtra("fragmentToLoad")

        when (fragmentToLoad) {
            "FriendRequestsFragment" -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.all, FriendRequestsFragment())
                    .commit()
            }
            "RatingFragment" -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.all, RatingFragment())
                    .commit()
            }
            else -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.all, MapsFragment())
                    .commit()
            }
        }
    }
}