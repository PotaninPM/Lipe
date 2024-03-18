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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.lipe.viewModels.AppViewModel
import com.example.lipe.chats.ChatsFragment
import com.example.lipe.create_events.CreateEventFragment
import com.example.lipe.database.EntEventModelDB
import com.example.lipe.databinding.FragmentMapsBinding
import com.example.lipe.view_events.EventFragment
import com.example.lipe.viewModels.EventEntVM
import com.example.lipe.viewModels.SaveStateMapsVM
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.measurement.api.AppMeasurementSdk.ConditionalUserProperty.NAME
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes.ADDRESS
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import java.lang.reflect.Field

class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null

    //View Model
    private lateinit var appVM: AppViewModel

    private lateinit var eventEntVM: EventEntVM

    private lateinit var saveStateMapVM: SaveStateMapsVM

    private val binding get() = _binding!!

    private var getReg: String ?= null

    private lateinit var mMap: GoogleMap

    private  lateinit var dbRef: DatabaseReference

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        dbRef = FirebaseDatabase.getInstance().getReference("current_events")

        // show all markers on map
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

        mMap.setOnMarkerClickListener { marker ->

            val markerPosition = marker.position

            val latitude = markerPosition.latitude
            val longitude = markerPosition.longitude

            searchEvent(latitude, longitude) { ready ->
                EventFragment.show(childFragmentManager)
            }

            true
        }
    }


    //get full info about event and write to VM

    //callback: (event: EntEventModelDB) -> Unit

    //search event by coordinates
    private fun searchEvent(coord1: Double, coord2: Double, callback: (ready: Boolean) -> Unit) {
        dbRef = FirebaseDatabase.getInstance().getReference("current_events")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(eventSnapshot in dataSnapshot.children) {
                    val coordinates: List<Double>? = eventSnapshot.child("coordinates").getValue(object : GenericTypeIndicator<List<Double>>() {})

                    if (coordinates!![0] == coord1 && coordinates[1] == coord2) {
                        var type = eventSnapshot.child("type_of_event").value.toString()
                        if(type == "ent") {
                            var id: Int = Integer.valueOf(eventSnapshot.child("event_id").value.toString())
                            var maxPeople: Int = eventSnapshot.child("max_people").value.toString().toInt()
                            var title = eventSnapshot.child("title").value.toString()
                            val description = eventSnapshot.child("description").value.toString()
                            val creator_id = eventSnapshot.child("creator_id").value.toString()
                            var photos = listOf("1")
                            var peopleGo = listOf("1")
                            var adress = eventSnapshot.child("adress").value.toString()
                            var freePlaces = maxPeople - eventSnapshot.child("amount_reg_people").value.toString().toInt()
                            var time_of_creation = eventSnapshot.child("time_of_creation").value.toString()
                            val date_of_meeeting = eventSnapshot.child("date_of_meet").value.toString()
                            var type_sport = eventSnapshot.child("sport_type").value.toString()
                            var amount_reg_people:Int = Integer.valueOf(eventSnapshot.child("amount_reg_people").value.toString())

                            //eventEntVM.maxPeople = maxPeople

                            eventEntVM.setInfo(id, maxPeople, title, creator_id, photos, peopleGo, adress, freePlaces, description, time_of_creation, date_of_meeeting, type_sport, amount_reg_people)
                        } else if(type == "eco") {
                            //TODO
                        }
                        callback(true)
                        break
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError","Ошибка Firebase ${databaseError.message}")
            }
        })
    }


    private fun addMarker(latLng: LatLng) {
        mMap.addMarker(MarkerOptions().position(latLng))
        //?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.basketball_32))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        appVM = ViewModelProvider(requireActivity()).get(AppViewModel::class.java)
        eventEntVM = ViewModelProvider(requireActivity()).get(EventEntVM::class.java)
        saveStateMapVM = ViewModelProvider(requireActivity()).get(SaveStateMapsVM::class.java)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

//        val placeId = "ChIJgUbEo8cfqokR5lP9_Wh_DaM"
//        val placeFields = listOf(Place.Field.ID, Place.Field.NAME)
//
//        val placesClient: PlacesClient = Places.createClient(requireActivity())
//// Construct a request object, passing the place ID and fields array.
//        val request = FetchPlaceRequest.newInstance(placeId, placeFields)
//
//        placesClient.fetchPlace(request)
//            .addOnSuccessListener { response: FetchPlaceResponse ->
//                val place = response.place
//                Log.i("INFOG", "Place found: ${place.name}")
//            }.addOnFailureListener { exception: Exception ->
//                if (exception is ApiException) {
//                    Log.e("INFOG", "Place not found: ${exception.message}")
//                    val statusCode = exception.statusCode
//                }
//            }

        if(appVM.reg == "yes") {
            showSuccessRegWindow()
            appVM.reg = "no"
        }

//        binding.entEvents.setOnClickListener {
//            binding.entEvents.setBackgroundColor(R.drawable.chosen_type)
//        }


        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.profile -> replaceFragment(ProfileFragment())
                R.id.map -> replaceFragment(MapsFragment())
                R.id.rating -> replaceFragment(RatingFragment())
                R.id.chats -> replaceFragment(ChatsFragment())
                //R.id.add -> replaceFragment(GetPointsFragment())
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
