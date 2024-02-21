package com.example.lipe

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.example.lipe.databinding.FragmentMapsBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class MapsFragment : Fragment() {

    private lateinit var autocompleteFragment: AutocompleteSupportFragment

    private var _binding: FragmentMapsBinding? = null

    private val binding get() = _binding!!

    private var getReg: String ?= null

    private lateinit var mMap: GoogleMap

    private lateinit var bottomSheet: BottomSheetDialog

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        //val createEventBSheet = CreateEventFragment()
        //gMap.setOnMapClickListener { latLng ->
//        gMap.setOnMapClickListener {
//            Toast.makeText(requireContext(), "desdg", Toast.LENGTH_LONG).show()
//        }
        //val latLng = LatLng(55.75345893559696, 37.649131307444584)


        mMap.setOnMapLongClickListener { latLng ->
            CreateEventFragment.show(childFragmentManager)
        }
        //}
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

        val view = binding.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        if(getReg == "success_reg") {
            showSuccessRegWindow()
        }


        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.profile -> view.findNavController().navigate(R.id.action_mapsFragment_to_otherProfileFragment)
                else -> {

                }
            }
            true
        }
    }

    private fun showSuccessRegWindow() {
        val pop_up_menu = layoutInflater.inflate(R.layout.pop_up_notification_success_sign_up, null)
        val pop_up = Dialog(requireContext())
        pop_up.setContentView(pop_up_menu)
        pop_up.setCancelable(true)
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
