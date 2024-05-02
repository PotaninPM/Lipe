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
    private var appInForeground = false
    override fun onCreate() {
        super.onCreate()

        auth = FirebaseAuth.getInstance()
//        if(auth.currentUser!!.uid != null) {
//            setUserStatus("online")
//        }
        val user = auth.currentUser!!.uid

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {
                if(user != null) {
                    setUserStatus("online")
                }
                Log.d("INFOG", "activityStarted")
            }

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {
                if(user != null) {
                    setUserStatus("offline")
                }
                Log.d("INFOG", "activityStopped")
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                Log.d("INFOG", "activityDestroyed")
            }
        })
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