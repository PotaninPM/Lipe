package com.example.lipe

import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.lipe.databinding.FragmentEventEntBinding
import com.example.lipe.databinding.FragmentProfileBinding
import com.example.lipe.sign_up_in.SignUpFragment
import com.example.lipe.viewModels.ProfileVM
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private  lateinit var dbRef: DatabaseReference

    private lateinit var auth: FirebaseAuth

    private val profileVM: ProfileVM by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = auth.currentUser?.uid

        //if(profileVM.ratingPoints.value ==) {
        findAccount() {ready ->
            if(ready) {
                Log.d("INFOG", profileVM.nameLastName.value.toString())
            }
        }
        //}

        binding.getPoints.setOnClickListener {
            auth.signOut()
        }




//        binding.more.setOnClickListener {
//
//        }
        switchTabs(0)
//        binding.bottomNavigation.setOnItemSelectedListener {
//            when(it.itemId) {
//                R.id.map -> view.findNavController().navigate(R.id.action_profileFragment_to_mapsFragment)
//
//                else -> {
//
//                }
//            }
//            true
//        }


    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        dbRef = FirebaseDatabase.getInstance().getReference("users")
        auth = FirebaseAuth.getInstance()

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = profileVM
        }

        val view = binding.root
        return view
    }
    private fun findAccount(callback: (ready: Boolean) -> Unit) {

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(profile in dataSnapshot.children) {
                    if(profile.child("uid").value.toString() == auth.currentUser!!.uid) {
                        val name:String = profile.child("firstName").value.toString()
                        val lastName:String = profile.child("lastName").value.toString()
                        val ratingAmount: Int = profile.child("rating").value.toString().toInt()
                        val friendsAmount: Int = profile.child("friends_amount").value.toString().toInt()
                        val eventsAmount: Int = profile.child("events_amount").value.toString().toInt()
                        val firstLastName: String = name + " " + lastName
                        profileVM.setInfo(firstLastName, friendsAmount,eventsAmount, ratingAmount)

                        callback(true)
                        break
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError","Ошибка Firebase ${databaseError.message}")
                callback(false)
            }
        })
    }

    private fun switchTabs(position: Int) {
        val fragment = when(position) {
            0 -> CurEventsInProfileFragment()
            1 -> RatingFragment()
            2 -> SignUpFragment()
            else -> null
        }

        fragment?.let {
            childFragmentManager.beginTransaction()
                .replace(R.id.switcher, it)
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}