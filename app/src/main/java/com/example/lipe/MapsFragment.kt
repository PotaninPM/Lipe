package com.example.lipe

import ProfileFragment
import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.lipe.chats_and_groups.ChatsAndGroupsFragment
import com.example.lipe.viewModels.AppVM
import com.example.lipe.create_events.CreateEventFragment
import com.example.lipe.databinding.FragmentMapsBinding
import com.example.lipe.rating_board.RatingFragment
import com.example.lipe.viewModels.EventEcoVM
import com.example.lipe.view_events.EventFragment
import com.example.lipe.viewModels.EventEntVM
import com.example.lipe.viewModels.SaveStateMapsVM
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlin.math.roundToLong

class MapsFragment : Fragment(), OnMapReadyCallback, LocationListener {

    private var _binding: FragmentMapsBinding? = null

    //View Model
    private lateinit var appVM: AppVM

    private val eventEntVM: EventEntVM by activityViewModels()

    private lateinit var eventEcoVM: EventEcoVM

    private lateinit var saveStateMapVM: SaveStateMapsVM

    private lateinit var locationManager: LocationManager

    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null

    private lateinit var dbRef_user: DatabaseReference
    private lateinit var dbRef_event: DatabaseReference

    private val ecoEventsMarkersMap = HashMap<String, Marker>()
    private val entEventsMarkersMap = HashMap<String, Marker>()
    private val helpEventsMarkersMap = HashMap<String, Marker>()

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }

        dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")
        // show all markers on map
        dbRef_event.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val coordinates: List<Double>? = dataSnapshot.child("coordinates").getValue(object : GenericTypeIndicator<List<Double>>() {})
                val eventId: String = dataSnapshot.child("event_id").value.toString()
                val type = dataSnapshot.child("type_of_event").value?.toString() ?: "def_type"

                var sport_type = "-"

                if(type == "ent") {
                    sport_type = dataSnapshot.child("sport_type").value.toString()
                }
                if (coordinates != null) {
                    addMarker(LatLng(coordinates[0], coordinates[1]), type, eventId, sport_type)
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val eventId: String = dataSnapshot.child("event_id").value.toString()
                val type: String = dataSnapshot.child("type_of_event").value.toString()
                if(type == "ent") {
                    entEventsMarkersMap[eventId]?.remove()
                    entEventsMarkersMap.remove(eventId)
                } else if(type == "eco") {
                    ecoEventsMarkersMap[eventId]?.remove()
                    ecoEventsMarkersMap.remove(eventId)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                // Обработка изменения события
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                // Обработка перемещения события
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

            eventEntVM.latitude = latitude
            eventEntVM.longtitude = longitude

            appVM.latitude = latitude
            appVM.longtitude = longitude

            searchTypeOfEvent(latitude, longitude) {ready ->
                if(ready) {
                    EventFragment.show(childFragmentManager)
                }
            }

            true
        }
    }

    override fun onLocationChanged(location: Location) {
        // update cur coordinates
        Log.d("INFOG", "1")
        currentLocation = location
        currentLatitude = location.latitude
        currentLongitude = location.longitude
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)
        eventEcoVM = ViewModelProvider(requireActivity()).get(EventEcoVM::class.java)
        saveStateMapVM = ViewModelProvider(requireActivity()).get(SaveStateMapsVM::class.java)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        binding.allEvents.setBackgroundResource(R.drawable.vary_of_events)
        binding.allText.setTextColor(Color.BLACK)

        if(appVM.reg == "yes") {
            showSuccessRegWindow()
            appVM.reg = "no"
        }

        binding.friends.setOnClickListener {
            if (appVM.markersType != "friends") {
                appVM.markersType = "friends"
                resetBackgroundForBtns()
                showMarkersByType("friends")
                binding.friends.setBackgroundResource(R.drawable.vary_of_events)
                binding.friendsText.setTextColor(Color.BLACK)
            }
        }

        binding.allEvents.setOnClickListener {
            if (appVM.markersType != "all") {
                appVM.markersType = "all"
                resetBackgroundForBtns()
                showMarkersByType("all")
                binding.allEvents.setBackgroundResource(R.drawable.vary_of_events)
                binding.allText.setTextColor(Color.BLACK)
            }
        }

        binding.ecoEvents.setOnClickListener {
            if (appVM.markersType != "eco") {
                appVM.markersType = "eco"
                resetBackgroundForBtns()
                showMarkersByType("eco")
                binding.ecoEvents.setBackgroundResource(R.drawable.vary_of_events)
                binding.ecoText.setTextColor(Color.BLACK)
            }
        }

        binding.entEvents.setOnClickListener {
            if (appVM.markersType != "ent") {
                appVM.markersType = "ent"
                resetBackgroundForBtns()
                showMarkersByType("ent")
                binding.entEvents.setBackgroundResource(R.drawable.vary_of_events)
                binding.entText.setTextColor(Color.BLACK)
            }
        }

        binding.helpEvents.setOnClickListener {
            if (appVM.markersType != "help") {
                appVM.markersType = "help"
                resetBackgroundForBtns()
                showMarkersByType("help")
                binding.helpEvents.setBackgroundResource(R.drawable.vary_of_events)
                binding.helpText.setTextColor(Color.BLACK)
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.profile -> replaceFragment(ProfileFragment())
                R.id.map -> replaceFragment(MapsFragment())
                R.id.rating -> replaceFragment(RatingFragment())
                R.id.chats -> replaceFragment(ChatsAndGroupsFragment())
                else -> {

                }
            }
            true
        }
    }

    private fun searchTypeOfEvent(coord1: Double, coord2: Double, callback: (ready: Boolean) -> Unit) {
        val dbRefEvent = FirebaseDatabase.getInstance().getReference("current_events")

        dbRefEvent.addListenerForSingleValueEvent(object : ValueEventListener {
            var done = 0
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(eventSnapshot in dataSnapshot.children) {
                    val coordinates: List<Double>? = eventSnapshot.child("coordinates").getValue(object : GenericTypeIndicator<List<Double>>() {})
                    if(coordinates != null && coordinates[0] == coord1 && coordinates[1] == coord2) {
                        val type = eventSnapshot.child("type_of_event").value.toString()
                        val eventId: String = eventSnapshot.child("event_id").value.toString()
                        if(type == "ent") {
                            val type_sport:String = eventSnapshot.child("sport_type").value.toString()
                            appVM.type_sport = type_sport
                        }
                        appVM.type = type
                        done = 1
                        callback(true)
                        break
                    }
                }
                if(done != 1) {
                    callback(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Ошибка Firebase ${databaseError.message}")
            }
        })
    }

    private fun addMarker(latLng: LatLng, type: String, eventId: String, sport_type: String): Marker? {
        var marker: Marker? = null
        val markerLayout = LayoutInflater.from(requireContext()).inflate(R.layout.custom_marker, null)
        val markerImageView = markerLayout.findViewById<ImageView>(R.id.imageView)

        var sportType: Int = 0

        if(sport_type != "-") {
            if(sport_type == "Воллейбол") {
                sportType = R.drawable.volleyball_2
            } else if(sport_type == "Футбол") {
                sportType = R.drawable.football
            } else if(sport_type == "Рэгби") {
                sportType = R.drawable.rugby_ball
            } else if(sport_type == "Баскетбол") {
                sportType = R.drawable.img_basketballimg
            } else if(sport_type == "Теннис") {
                sportType = R.drawable.tennis
            } else if(sport_type == "Лыжи") {
                sportType = R.drawable.skiing_1
            }
        }
        val markerImageResource = when (type) {
            "eco" -> R.drawable.leaf
            "ent" -> sportType
            else -> R.drawable.basketball_32
        }

        markerImageView.setImageResource(markerImageResource)

        val markerOptions = MarkerOptions()
            .position(latLng)
            .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(markerLayout)))
            .anchor(0.5f, 1f)

        marker = mMap.addMarker(markerOptions)

        when (type) {
            "eco" -> ecoEventsMarkersMap[eventId] = marker!!
            "ent" -> entEventsMarkersMap[eventId] = marker!!
            "help" -> helpEventsMarkersMap[eventId] = marker!!
        }

        return marker
    }
    private fun showMarkersByType(type: String) {
        when (type) {
            "all" -> {
                showOrHideMarkers(ecoEventsMarkersMap, "show")
                showOrHideMarkers(entEventsMarkersMap, "show")
                showOrHideMarkers(helpEventsMarkersMap, "show")
            }
            "eco" -> {
                showOrHideMarkers(ecoEventsMarkersMap, "show")
                showOrHideMarkers(helpEventsMarkersMap, "hide")
                showOrHideMarkers(entEventsMarkersMap, "hide")
            }
            "ent" -> {
                showOrHideMarkers(entEventsMarkersMap, "show")
                showOrHideMarkers(helpEventsMarkersMap, "hide")
                showOrHideMarkers(ecoEventsMarkersMap, "hide")
            }
            "friends" -> {
                showOrHideMarkers(entEventsMarkersMap, "hide")
                showOrHideMarkers(helpEventsMarkersMap, "hide")
                showOrHideMarkers(ecoEventsMarkersMap, "hide")
            }
            "help" -> {
                showOrHideMarkers(helpEventsMarkersMap, "show")
                showOrHideMarkers(entEventsMarkersMap, "hide")
                showOrHideMarkers(ecoEventsMarkersMap, "hide")
            }
        }
    }
    private fun showOrHideMarkers(markerMap: HashMap<String, Marker>, func: String) {
        if(func != "hide")
            markerMap.forEach { it.value.isVisible = true }
        else
            markerMap.forEach { it.value.isVisible = false }
    }
    private fun createDrawableFromView(view: View): Bitmap {
        val displayMetrics = resources.displayMetrics
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun resetBackgroundForBtns() {
        val type = appVM.markersType
        binding.friends.setBackgroundResource(if(type == "friends") R.drawable.vary_of_events else 0)
        binding.allEvents.setBackgroundResource(if(type == "all") R.drawable.vary_of_events else 0)
        binding.ecoEvents.setBackgroundResource(if(type == "eco") R.drawable.vary_of_events else 0)
        binding.entEvents.setBackgroundResource(if(type == "ent") R.drawable.vary_of_events else 0)
        binding.helpEvents.setBackgroundResource(if(type == "help") R.drawable.vary_of_events else 0)
    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.map, fragment)
        fragmentTransaction.commit()
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        val backCallback = object: OnBackPressedCallback(true){
//            override fun handleOnBackPressed() {
//                if(childFragmentManager.backStackEntryCount > 1) {
//                    childFragmentManager.popBackStack()
//                }
//                parentFragmentManager.popBackStack()
//            }
//        }
//        activity?.onBackPressedDispatcher?.addCallback(this,backCallback)
//    }


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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                mMap.isMyLocationEnabled = true
            } else {
                // Показать диалоговое окно с объяснением, почему это разрешение необходимо
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    override fun onDestroyView() {
        super.onDestroyView()
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    override fun onMapReady(p0: GoogleMap) {
        TODO("Not yet implemented")
    }

}
