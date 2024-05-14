package com.example.lipe

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.FirebaseDatabase.*

class SetOnlineOrOffline : Application() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate() {
        super.onCreate()

        try {
            auth = FirebaseAuth.getInstance()

            val user = auth.currentUser?.uid

            Log.d("INFOG1", user.toString())

            if(user != "null") {
                registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
                    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

                    override fun onActivityStarted(activity: Activity) {
                        if (user != null) {
                            setUserStatus("online")
                        }
                        Log.d("INFOG", "activityStarted")
                    }

                    override fun onActivityResumed(activity: Activity) {}

                    override fun onActivityPaused(activity: Activity) {
                        if (user != null) {
                            setUserStatus("offline")
                        }
                    }

                    override fun onActivityStopped(activity: Activity) {
                        if (user != null) {
                            setUserStatus("offline")
                        }
                        Log.d("INFOG", "activityStopped")
                    }

                    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

                    override fun onActivityDestroyed(activity: Activity) {
                        if (user != null) {
                            setUserStatus("offline")
                        }
                        Log.d("INFOG", "activityDestroyed")
                    }
                })
            }
        } catch (e: Exception) {
            Log.e("INFOG1", e.message.toString())
        }
    }

    private fun setUserStatus(status: String) {
        val dbRef_user = FirebaseDatabase.getInstance()
            .getReference("users/${auth.currentUser!!.uid}")
        dbRef_user.child("status").setValue(status).addOnSuccessListener {
            Log.d("INFOG", "user ${status}")
        }.addOnFailureListener {

        }
    }

}