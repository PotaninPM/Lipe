package com.example.lipe

import ProfileFragment
import android.Manifest
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.request.ImageRequest
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
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapsBinding? = null

    //View Model
    private lateinit var appVM: AppVM

    private val eventEntVM: EventEntVM by activityViewModels()

    private lateinit var eventEcoVM: EventEcoVM

    private lateinit var saveStateMapVM: SaveStateMapsVM

    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback

    private var myLocationMarker: Marker? = null

    private lateinit var dbRef_user: DatabaseReference
    private lateinit var dbRef_event: DatabaseReference
    private lateinit var dbRef_friends: DatabaseReference
    private lateinit var dbRef_status: DatabaseReference

    private lateinit var auth: FirebaseAuth

    private val ecoEventsMarkersMap = HashMap<String, Marker>()
    private val entEventsMarkersMap = HashMap<String, Marker>()
    private val helpEventsMarkersMap = HashMap<String, Marker>()
    private val friendsMarkersMap = HashMap<String, Marker>()

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    private fun setMapStyle() {
        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val mapStyleResourceId = if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
            R.raw.map_style_dark
        } else {
            R.raw.map_style_light
        }

        try {
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    mapStyleResourceId
                )
            )

            if (!success) {
                Log.e("INFOG", "failed")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("INFOG", "Error: ", e)
        }
    }

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap

        setMapStyle()

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            addAllEvents()

            addAllFriends()
        } else {
            Toast.makeText(requireContext(), "Вы не авторизован", Toast.LENGTH_LONG).show()
        }

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

            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(latitude, longitude),
                    17f
                )
            )

            searchTypeOfEvent(latitude, longitude) {ready ->
                if(ready) {
                    EventFragment.show(childFragmentManager)
                }
            }

            true
        }
    }

    private fun addAllFriends() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            dbRef_friends.child("users").child(currentUser.uid).child("friends")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                        val friendUid = dataSnapshot.value.toString()
                        addFriendToMap(friendUid)
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {}

                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                        val friendUid = dataSnapshot.value.toString()
                        removeFriendFromMap(friendUid)
                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {}

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("FirebaseError", "Ошибка ${databaseError.message}")
                    }
                })
        } else {
            Toast.makeText(requireContext(), "Вы не авторизован", Toast.LENGTH_LONG).show()
        }
    }
    private fun removeFriendFromMap(friendUid: String) {
        val existingMarker = friendsMarkersMap[friendUid]
        existingMarker?.remove()
        friendsMarkersMap.remove(friendUid)
    }

    private fun addFriendToMap(friendUid: String) {
        val locationListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val latitude = dataSnapshot.child("latitude").value.toString()
                val longitude = dataSnapshot.child("longitude").value.toString()
                if ((latitude != "null" && longitude != "null") && (latitude != "-" && longitude != "-")) {
                    val latLng = LatLng(latitude.toDouble(), longitude.toDouble())
                    updateFriendMarkerPosition(friendUid, latLng)

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Ошибка Firebase ${databaseError.message}")
            }
        }

        val statusListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val status = dataSnapshot.value as? String
                if (status != null) {
                    val marker = friendsMarkersMap[friendUid]
                    if (marker != null) {
                        //updateMarkerStatusView(marker, status)
                    }
                }
                Log.d("INFOG", "checkStatus $status")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Ошибка Firebase ${databaseError.message}")
            }
        }

        val dbRef_location = FirebaseDatabase.getInstance().getReference("location/${friendUid}")
        dbRef_location.addValueEventListener(locationListener)
        val dbRef_status = FirebaseDatabase.getInstance().getReference("users/${friendUid}/status")
        dbRef_status.addValueEventListener(statusListener)
    }



    //smth wrong with that function, for tommorow
//    private fun updateMarkerStatusView(marker: Marker?, status: String) {
//        marker?.let { marker ->
//            val markerLayout = marker.tag as? View
//            if (markerLayout != null) {
//                val statusView = markerLayout.findViewById<View>(R.id.statusView)
//                when (status) {
//                    "online" -> statusView.setBackgroundResource(R.drawable.online_spot)
//                    "offline" -> statusView.setBackgroundResource(R.drawable.offline_spot)
//                }
//            }
//        }
//    }

    private fun addAllEvents() {
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

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError","Ошибка Firebase ${databaseError.message}")
            }
        })
    }

    private fun updateFriendMarkerPosition(friendUid: String, latLng: LatLng) {
        val existingMarker = friendsMarkersMap[friendUid]
        if (existingMarker != null) {
            val startPosition = existingMarker.position
            val endPosition = latLng

            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            valueAnimator.duration = 1000
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.addUpdateListener { animation ->
                val v = animation.animatedFraction
                val newPosition = LatLng(
                    startPosition.latitude * (1 - v) + endPosition.latitude * v,
                    startPosition.longitude * (1 - v) + endPosition.longitude * v
                )
                existingMarker.position = newPosition
            }
            valueAnimator.start()
        } else {
            val markerLayout = LayoutInflater.from(requireContext()).inflate(R.layout.custom_marker_friend, null)
            val markerImageView = markerLayout.findViewById<ImageView>(R.id.imageView)

            val storage = FirebaseStorage.getInstance().getReference("avatars/$friendUid")
            storage.downloadUrl.addOnSuccessListener { url ->
                lifecycleScope.launch {
                    val markerOptions = MarkerOptions()
                        .position(latLng)
                        .icon(
                            BitmapDescriptorFactory.fromBitmap(
                                createDrawableFromView(
                                    markerLayout
                                )
                            )
                        )
                        .anchor(0.5f, 1f)

                    val marker = mMap.addMarker(markerOptions)

                    val startPosition = marker!!.position
                    val endPosition = latLng

                    val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
                    valueAnimator.duration = 1000
                    valueAnimator.interpolator = LinearInterpolator()
                    valueAnimator.addUpdateListener { animation ->
                        val v = animation.animatedFraction
                        val newPosition = LatLng(
                            startPosition.latitude * (1 - v) + endPosition.latitude * v,
                            startPosition.longitude * (1 - v) + endPosition.longitude * v
                        )
                        marker.position = newPosition
                    }
                    valueAnimator.start()

                    friendsMarkersMap[friendUid] = marker
                }
            }
        }
    }

    private fun startLocationUpdates() {
        val dbRef_location = FirebaseDatabase.getInstance().getReference("location/${auth.currentUser!!.uid}")

        val markerLayout = LayoutInflater.from(requireContext()).inflate(R.layout.custom_marker_friend, null)
        val markerImageView = markerLayout.findViewById<ImageView>(R.id.imageView)

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    if(myLocationMarker == null) {
                        val newLocation = hashMapOf<String, Double>(
                            "latitude" to location.latitude,
                            "longitude" to location.longitude
                        )
                        dbRef_location.setValue(newLocation).addOnSuccessListener {
                            Log.d("INFOG", "locUpdate")
                        }
                        val storage = FirebaseStorage.getInstance().getReference("avatars/${auth.currentUser!!.uid}")
                        storage.downloadUrl.addOnSuccessListener { url ->
                            lifecycleScope.launch {
                                val bitmap: Bitmap = withContext(Dispatchers.IO) {
                                    Coil.imageLoader(requireContext()).execute(
                                        ImageRequest.Builder(requireContext())
                                            .data(url)
                                            .build()
                                    ).drawable?.toBitmap()!!
                                }

                                markerImageView.setImageBitmap(bitmap)

                                val markerOptions = MarkerOptions()
                                    .position(LatLng(location.latitude, location.longitude))
                                    .icon(
                                        BitmapDescriptorFactory.fromBitmap(
                                            createDrawableFromView(
                                                markerLayout
                                            )
                                        )
                                    )
                                    .anchor(0.5f, 1f)

                                myLocationMarker = mMap.addMarker(markerOptions)
                            }
                        }

                    } else {
                        val newLocation = hashMapOf<String, Double>(
                            "latitude" to location.latitude,
                            "longitude" to location.longitude
                        )
                        dbRef_location.setValue(newLocation).addOnSuccessListener {

                        }
                        val startPosition = myLocationMarker!!.position
                        val endPosition = LatLng(location.latitude, location.longitude)

                        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
                        valueAnimator.duration = 1000
                        valueAnimator.interpolator = LinearInterpolator()
                        valueAnimator.addUpdateListener { animation ->
                            val v = animation.animatedFraction
                            val newPosition = LatLng(
                                startPosition.latitude * (1 - v) + endPosition.latitude * v,
                                startPosition.longitude * (1 - v) + endPosition.longitude * v
                            )
                            myLocationMarker!!.position = newPosition
                        }
                        valueAnimator.start()
                    }
                }
            }
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            mMap.isMyLocationEnabled = false
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)


        auth = FirebaseAuth.getInstance()
        appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)
        eventEcoVM = ViewModelProvider(requireActivity()).get(EventEcoVM::class.java)
        saveStateMapVM = ViewModelProvider(requireActivity()).get(SaveStateMapsVM::class.java)

        Log.d("INFOG", auth.currentUser!!.uid)

        dbRef_user = FirebaseDatabase.getInstance().getReference()
        dbRef_friends = FirebaseDatabase.getInstance().getReference()
        dbRef_status = FirebaseDatabase.getInstance().getReference()

        findPersonOnMap()

        val view = binding.root
        return view
    }

    private fun findPersonOnMap() {
        val dbRef_location = FirebaseDatabase.getInstance().getReference("location/${auth.currentUser!!.uid}")
        dbRef_location.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latitude = snapshot.child("latitude").value.toString()
                val longitude = snapshot.child("longitude").value.toString()

                if(latitude != "-" && longitude != "-") {
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(latitude.toDouble(), longitude.toDouble()),
                            16f
                        )
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        binding.allEvents.setBackgroundResource(R.drawable.vary_of_events)
        binding.allText.setTextColor(Color.BLACK)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

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
        val markerLayout = LayoutInflater.from(requireContext()).inflate(R.layout.custom_marker_event, null)
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
                showOrHideMarkers(friendsMarkersMap, "show")
            }
            "eco" -> {
                showOrHideMarkers(ecoEventsMarkersMap, "show")
                showOrHideMarkers(helpEventsMarkersMap, "hide")
                showOrHideMarkers(entEventsMarkersMap, "hide")
                showOrHideMarkers(friendsMarkersMap, "hide")
            }
            "ent" -> {
                showOrHideMarkers(entEventsMarkersMap, "show")
                showOrHideMarkers(helpEventsMarkersMap, "hide")
                showOrHideMarkers(ecoEventsMarkersMap, "hide")
                showOrHideMarkers(friendsMarkersMap, "hide")
            }
            "friends" -> {
                showOrHideMarkers(entEventsMarkersMap, "hide")
                showOrHideMarkers(helpEventsMarkersMap, "hide")
                showOrHideMarkers(ecoEventsMarkersMap, "hide")
                showOrHideMarkers(friendsMarkersMap, "show")
            }
            "help" -> {
                showOrHideMarkers(helpEventsMarkersMap, "show")
                showOrHideMarkers(entEventsMarkersMap, "hide")
                showOrHideMarkers(ecoEventsMarkersMap, "hide")
                showOrHideMarkers(friendsMarkersMap, "hide")
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
        val spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(spec, spec)
        val measuredWidth = view.measuredWidth
        val measuredHeight = view.measuredHeight
        view.layout(0, 0, measuredWidth, measuredHeight)
        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onPause() {
        super.onPause()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
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
        mMap = p0
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isMyLocationButtonEnabled = false
    }

}
