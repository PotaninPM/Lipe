package com.example.lipe

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.replace
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.lipe.ViewModels.AppViewModel
import com.example.lipe.databinding.FragmentMapsBinding

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

data class LatLngModel(
    val latitude: Double,
    val longitude: Double
)

class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null

    //View Model
    private lateinit var appVM: AppViewModel

    private val binding get() = _binding!!

    private var getReg: String ?= null

    private lateinit var mMap: GoogleMap

    private lateinit var bottomSheet: BottomSheetDialog

    private  lateinit var dbRef: DatabaseReference

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        dbRef = FirebaseDatabase.getInstance().getReference("current_events")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(eventSnapshot in dataSnapshot.children) {
                    val coordinates: List<Double>? = eventSnapshot.child("coordinates").getValue(object : GenericTypeIndicator<List<Double>>() {})

                    if (coordinates != null) {
                        addMarker(LatLng(coordinates[0], coordinates[1]))
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError","Ошибка Firebase ${databaseError.message}")
            }
        })

        mMap.setOnMapLongClickListener { latLng ->
            appVM.setCoord(latLng.latitude, latLng.longitude)
            CreateEventFragment.show(childFragmentManager)
        }
        //}
    }

    private fun addMarker(latLng: LatLng) {
        mMap.addMarker(MarkerOptions().position(latLng))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getReg = arguments?.getString("SignUpNew")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        appVM = ViewModelProvider(requireActivity()).get(AppViewModel::class.java)
        val view = binding.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        if(appVM.reg == "yes") {
            showSuccessRegWindow()
            appVM.reg = "no"
        }


        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.profile -> replaceFragment(ProfileFragment())
                R.id.map -> replaceFragment(MapsFragment())
                R.id.rating -> replaceFragment(RatingFragment())
                //R.id.chats -> replaceFragment()
                else -> {

                }
            }
            true
        }
    }



    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.map, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val backCallback = object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(childFragmentManager.backStackEntryCount > 1) {
                    childFragmentManager.popBackStack()
                }
                parentFragmentManager.popBackStack()
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(this,backCallback)
    }


    private fun showSuccessRegWindow() {
        val pop_up_menu = layoutInflater.inflate(R.layout.pop_up_notification_success_sign_up, null)
        val pop_up = Dialog(requireContext())
        pop_up.setContentView(pop_up_menu)
        pop_up.setCancelable(false)
        pop_up.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        pop_up.show()
        val closeBtn = pop_up.findViewById<Button>(R.id.close_ad)
        val goTraneBtn = pop_up.findViewById<Button>(R.id.go_trane)
        closeBtn.setOnClickListener {
            pop_up.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
