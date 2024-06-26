package com.example.lipe

import ProfileFragment
import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.location.Location
import android.os.Build
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
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
import com.example.lipe.friend_on_map_dialog.FriendOnMapBottomSheetFragment
import com.example.lipe.rating_board.RatingFragment
import com.example.lipe.reports.report_fragment_for_moderators.ReportsForModeratorsFragment
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
import com.google.android.gms.maps.model.CameraPosition
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
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapsBinding

    private var currentFragment: Fragment? = null

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var appVM: AppVM

    private val eventEntVM: EventEntVM by activityViewModels()

    //ViewModel
    private lateinit var eventEcoVM: EventEcoVM

    private lateinit var saveStateMapVM: SaveStateMapsVM

    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback

    private var myLocationMarker: Marker? = null

    private lateinit var dbrefUser: DatabaseReference
    private lateinit var dbrefEvent: DatabaseReference
    private lateinit var dbrefFriends: DatabaseReference
    private lateinit var dbrefStatus: DatabaseReference

    private lateinit var auth: FirebaseAuth

    private val ecoEventsMarkersMap = HashMap<String, Marker>()
    private val entEventsMarkersMap = HashMap<String, Marker>()
    private val helpEventsMarkersMap = HashMap<String, Marker>()
    private val friendsMarkersMap = HashMap<String, Marker>()

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    private fun addMarker(latLng: LatLng, type: String, eventId: String, sport_type: String): Marker? {
        var marker: Marker? = null
        val markerLayout = LayoutInflater.from(requireContext()).inflate(R.layout.custom_marker_event, null)
        val markerImageView = markerLayout.findViewById<ImageView>(R.id.imageView)

        var sportType: Int = 0

        Log.d("INFOG", type)

        if (sport_type != "-") {
            sportType = when (sport_type) {
                "Basketball" -> R.drawable.img_basketballimg
                "Volleyball" -> R.drawable.volleyball_2
                "Football" -> R.drawable.football
                "Rugby" -> R.drawable.rugby_ball
                "Workout" -> R.drawable.weights
                "Tennis" -> R.drawable.tennis
                "Badminton" -> R.drawable.shuttlecock
                "Table tennis" -> R.drawable.table_tennis
                "Gymnastics" -> R.drawable.gymnastic_rings
                "Fencing" -> R.drawable.fencing
                "Jogging" -> R.drawable.running_shoe
                "Curling" -> R.drawable.curling
                "Hockey" -> R.drawable.ice_hockey
                "Ice skating" -> R.drawable.ice_skate
                "Skiing" -> R.drawable.skiing_1
                "Downhill skiing" -> R.drawable.skiing
                "Snowboarding" -> R.drawable.snowboarding
                "Table games" -> R.drawable.board_game
                "Mobile games" -> R.drawable.mobile_game
                "Chess" -> R.drawable.chess_2
                "Programming" -> R.drawable.programming
                else -> {
                    0
                }
            }
        }
        val markerImageResource = when (type) {
            "eco" -> R.drawable.planet_icon
            "ent" -> sportType
            "help" -> R.drawable.dollar
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

    private fun searchTypeOfEvent(coord1: Double, coord2: Double, callback: (ready: Boolean) -> Unit) {
        val dbRefEvent = FirebaseDatabase.getInstance().getReference("current_events")

        dbRefEvent.addListenerForSingleValueEvent(object : ValueEventListener {
            var done = 0
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(eventSnapshot in dataSnapshot.children) {
                    val coordinates: List<Double>? = listOf(eventSnapshot.child("coordinates").child("latitude").value.toString().toDouble(), eventSnapshot.child("coordinates").child("longitude").value.toString().toDouble())
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
        if(isAdded) {
            mMap = googleMap

            setMapStyle()

            val currentUser = auth.currentUser
            if (currentUser != null) {
                addAllEvents()

                addAllFriends()
            } else {
                Toast.makeText(requireContext(), "Вы не авторизован", Toast.LENGTH_LONG).show()
            }

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


            mMap.setOnMapLongClickListener { latLng ->
                appVM.setCoord(latLng.latitude, latLng.longitude)
                CreateEventFragment.show(childFragmentManager)
            }

            mMap.setOnMarkerClickListener { marker ->

                if(marker.title != null) {
                    appVM.type = marker.title.toString()

                    val friendOnMapDialogFragment = FriendOnMapBottomSheetFragment()
                    friendOnMapDialogFragment.show(childFragmentManager, "FriendOnMapDialogFragment")

                } else {
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

                    searchTypeOfEvent(latitude, longitude) { ready ->
                        if (ready) {
                            EventFragment.show(childFragmentManager)
                        }
                    }
                }

                true
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        mMap.uiSettings.isMyLocationButtonEnabled = false

        val sharedPref = activity?.getSharedPreferences("userRef", Context.MODE_PRIVATE)

        val lat = sharedPref?.getFloat("latitude_user", 0f)
        val lng = sharedPref?.getFloat("longitude_user", 0f)
        val zoom = sharedPref?.getFloat("zoom_user", 0f)

        if (lat != null && lng != null && zoom != null && lat != 0f && lng != 0f && zoom != 0f) {
            val savedPosition = LatLng(lat.toDouble(), lng.toDouble())
            val cameraPosition = CameraPosition.Builder()
                .target(savedPosition)
                .zoom(zoom)
                .build()
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

        map.setOnCameraIdleListener {
            val currentPosition = map.cameraPosition.target
            val currentZoom = map.cameraPosition.zoom
            saveMapState(currentPosition, currentZoom)
        }
    }

    private fun saveMapState(position: LatLng, zoom: Float) {
        val sharedPref = activity?.getSharedPreferences("userRef", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putFloat("latitude_user", position.latitude.toFloat())
            putFloat("longitude_user", position.longitude.toFloat())
            putFloat("zoom_user", zoom)
            apply()
        }
    }


//    private fun startLocationService() {
//        if(isAdded && context != null) {
//            val serviceIntent = Intent(requireContext(), LocationService::class.java)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                ContextCompat.startForegroundService(requireContext(), serviceIntent)
//            } else {
//                requireContext().startService(serviceIntent)
//            }
//        }
//    }

    private fun addAllFriends() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            dbrefFriends.child("users").child(currentUser.uid).child("friends")
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
        if (!isAdded) return
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
                val status = dataSnapshot.value.toString()
                Log.d("INFOG", "friend: $friendUid status: $status")
                if (status != "null" && friendsMarkersMap[friendUid] != null) {
                    updateMarkerStatusView(friendUid, status)
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

        Coil.imageLoader(requireContext()).memoryCache?.clear()
    }


    private fun updateMarkerStatusView(friendUid: String, status: String) {
        try {
            val marker = friendsMarkersMap[friendUid]
            val markerLayout = marker?.tag as? View
            if (markerLayout != null) {
                val statusView = markerLayout.findViewById<View>(R.id.statusView)
                if(status == "online") {
                    statusView.setBackgroundResource(R.drawable.online_spot)
                } else {
                    statusView.setBackgroundResource(R.drawable.offline_spot)
                }

                marker.setIcon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(markerLayout)))
            }
        } catch (e: Exception) {
            Log.e("INFOG", e.message.toString())
        }
    }

    private fun addAllEvents() {
        dbrefEvent = FirebaseDatabase.getInstance().getReference("current_events")
        // show all markers on map
        dbrefEvent.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                try {
                    val coordinates: ArrayList<Double>? = arrayListOf(
                        dataSnapshot.child("coordinates").child("latitude").value.toString().toDouble(),
                        dataSnapshot.child("coordinates").child("longitude").value.toString().toDouble()
                    )
                    val eventId: String = dataSnapshot.child("event_id").value.toString()
                    val type = dataSnapshot.child("type_of_event").value?.toString() ?: "def_type"

                    Log.d("INFOG", "${coordinates?.get(0)} ${coordinates?.get(1)}")
                    var sport_type = "-"

                    if (type == "ent") {
                        sport_type = dataSnapshot.child("sport_type").value.toString()
                    }
                    if (coordinates != null) {
                        if (isAdded) {
                            addMarker(LatLng(coordinates[0], coordinates[1]), type, eventId, sport_type)
                        } else {
                            Log.e("MapsFragment", "Fragment not attached to a context.")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseError", "Error adding marker: ${e.message}")
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                try {
                    val eventId: String = dataSnapshot.child("event_id").value.toString()
                    val type: String = dataSnapshot.child("type_of_event").value.toString()
                    when (type) {
                        "ent" -> {
                            entEventsMarkersMap[eventId]?.remove()
                            entEventsMarkersMap.remove(eventId)
                        }
                        "eco" -> {
                            ecoEventsMarkersMap[eventId]?.remove()
                            ecoEventsMarkersMap.remove(eventId)
                        }
                        "help" -> {
                            helpEventsMarkersMap[eventId]?.remove()
                            helpEventsMarkersMap.remove(eventId)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseError", "Error removing marker: ${e.message}")
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                // Handle changes if needed
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                // Handle moves if needed
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Firebase error: ${databaseError.message}")
            }
        })
    }


    private fun updateFriendMarkerPosition(friendUid: String, latLng: LatLng) {
        try {
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

                val status_ref = FirebaseDatabase.getInstance().getReference("users/$friendUid/status")
                val storage = FirebaseStorage.getInstance().getReference("avatars/$friendUid")

                status_ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val status = snapshot.value.toString()
                        storage.downloadUrl.addOnSuccessListener { url ->
                            lifecycleScope.launch {
                                try {
                                    val bitmap: Bitmap = withContext(Dispatchers.IO) {
                                        val hardwareBitmap = Coil.imageLoader(requireContext()).execute(
                                            ImageRequest.Builder(requireContext())
                                                .data(url)
                                                .build()
                                        ).drawable?.toBitmap()!!

                                        getRoundedCornerBitmap(hardwareBitmap.copy(Bitmap.Config.ARGB_8888, true), 10)
                                    }

                                    markerImageView.setImageBitmap(bitmap)
                                    if(status == "online") {
                                        markerLayout.findViewById<View>(R.id.statusView).setBackgroundResource(R.drawable.online_spot)
                                    } else {
                                        markerLayout.findViewById<View>(R.id.statusView).setBackgroundResource(R.drawable.offline_spot)
                                    }

                                    val markerOptions = MarkerOptions()
                                        .position(latLng)
                                        .icon(
                                            BitmapDescriptorFactory.fromBitmap(
                                                createDrawableFromView(
                                                    markerLayout
                                                )
                                            )
                                        )
                                        .title(friendUid)
                                        .anchor(0.5f, 1f)

                                    val marker = mMap.addMarker(markerOptions)
                                    marker?.tag = markerLayout

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
                                } catch (e: Exception) {
                                    Log.e("INFOG", "Ошибка при загрузке изображения: ${e.message}")
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
        } catch (e : Exception) {
            Log.e("INFOG", "${e}")
        }
    }
    private fun startLocationUpdates() {
        try {
            val dbRefLocation = FirebaseDatabase.getInstance().getReference("location/${auth.currentUser!!.uid}")

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
                        if (myLocationMarker == null) {
                            createNewMyLocationMarker(location, markerLayout, markerImageView, dbRefLocation)
                        } else {
                            updateMyLocationMarker(location)
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
        } catch (e: Exception) {
            Log.e("INFOG", "${e.message}")
        }
    }

    private fun createNewMyLocationMarker(
        location: Location,
        markerLayout: View,
        markerImageView: ImageView,
        dbRefLocation: DatabaseReference
    ) {
        val storage = FirebaseStorage.getInstance().getReference("avatars/${auth.currentUser!!.uid}")
        storage.downloadUrl.addOnSuccessListener { url ->
            lifecycleScope.launch {
                try {
                    val bitmap: Bitmap = withContext(Dispatchers.IO) {
                        val hardwareBitmap = Coil.imageLoader(requireContext()).execute(
                            ImageRequest.Builder(requireContext())
                                .data(url)
                                .build()
                        ).drawable?.toBitmap()!!

                        getRoundedCornerBitmap(hardwareBitmap.copy(Bitmap.Config.ARGB_8888, true), 10)
                    }

                    markerImageView.setImageBitmap(bitmap)

                    val markerOptions = MarkerOptions()
                        .position(LatLng(location.latitude, location.longitude))
                        .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(markerLayout)))
                        .anchor(0.5f, 1f)

                    myLocationMarker = mMap.addMarker(markerOptions)

                    animateMarker(myLocationMarker!!, myLocationMarker!!.position, LatLng(location.latitude, location.longitude))

                    val newLocation = hashMapOf(
                        "latitude" to location.latitude,
                        "longitude" to location.longitude
                    )
                    dbRefLocation.setValue(newLocation).addOnSuccessListener {
                        Log.d("INFOG", "Локация обновлена")
                    }

                } catch (e: Exception) {
                    Log.e("INFOG", "Ошибка при загрузке изображения: ${e.message}")
                }
            }
        }
    }

    private fun updateMyLocationMarker(location: Location) {
        val startPosition = myLocationMarker!!.position
        val endPosition = LatLng(location.latitude, location.longitude)

        animateMarker(myLocationMarker!!, startPosition, endPosition)

        val newLocation = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude
        )
        FirebaseDatabase.getInstance().getReference("location/${auth.currentUser!!.uid}")
            .setValue(newLocation)
            .addOnSuccessListener {
                Log.d("INFOG", "Локация обновлена")
            }
    }

    private fun animateMarker(marker: Marker, startPosition: LatLng, endPosition: LatLng) {
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
    }

    private fun locationPermissions(): Boolean {
        val fineLocationPermission = ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationPermission = ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val foregroundServiceLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.FOREGROUND_SERVICE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return fineLocationPermission && coarseLocationPermission && foregroundServiceLocationPermission
    }

    fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val color = 0xff424242.toInt()
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        val roundPx = pixels.toFloat()

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapsBinding.inflate(inflater, container, false)

        if(isAdded) {
            currentFragment = MapsFragment()

            auth = FirebaseAuth.getInstance()
            appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)
            eventEcoVM = ViewModelProvider(requireActivity()).get(EventEcoVM::class.java)
            saveStateMapVM = ViewModelProvider(requireActivity()).get(SaveStateMapsVM::class.java)

            dbrefUser = FirebaseDatabase.getInstance().getReference()
            dbrefFriends = FirebaseDatabase.getInstance().getReference()
            dbrefStatus = FirebaseDatabase.getInstance().getReference()

            val mapFragment =
                childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
            mapFragment.getMapAsync(this)

            sharedPreferences = requireActivity().getSharedPreferences("userRef", Context.MODE_PRIVATE)
        }

        val view = binding.root
        return view
    }

    private fun isFirstTime(): Boolean {
        return sharedPreferences.getBoolean("enter", true)
    }

    private fun markAsVisited() {
        sharedPreferences.edit().putBoolean("enter", false).apply()
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

        if(auth.currentUser != null) {
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)
            binding.allEvents.setBackgroundResource(R.drawable.vary_of_events)

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

            binding.allText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            checkUsersRole()

            if (locationPermissions()) {
                //startLocationService()
            } else {
                requestLocationPermissions()
            }

            view.post {
                if(isAdded && context != null) {
                    if(isFirstTime()) {
                        BeginDialogFragment.newInstance()
                            .show(childFragmentManager, "BeginDialogFragment")
                        markAsVisited()
                    }
                }
            }


            binding.friends.setOnClickListener {
                if (appVM.markersType != "friends") {
                    appVM.markersType = "friends"
                    resetBackgroundForBtns()
                    showMarkersByType("friends")
                    binding.friends.setBackgroundResource(R.drawable.vary_of_events)
                }
            }

            binding.allEvents.setOnClickListener {
                if (appVM.markersType != "all") {
                    appVM.markersType = "all"
                    resetBackgroundForBtns()
                    showMarkersByType("all")
                    binding.allEvents.setBackgroundResource(R.drawable.vary_of_events)
                }
            }

            binding.ecoEvents.setOnClickListener {
                if (appVM.markersType != "eco") {
                    appVM.markersType = "eco"
                    resetBackgroundForBtns()
                    showMarkersByType("eco")
                    binding.ecoEvents.setBackgroundResource(R.drawable.vary_of_events)
                }
            }

            binding.entEvents.setOnClickListener {
                if (appVM.markersType != "ent") {
                    appVM.markersType = "ent"
                    resetBackgroundForBtns()
                    showMarkersByType("ent")
                    binding.entEvents.setBackgroundResource(R.drawable.vary_of_events)
                }
            }

            binding.helpEvents.setOnClickListener {
                if (appVM.markersType != "help") {
                    appVM.markersType = "help"
                    resetBackgroundForBtns()
                    showMarkersByType("help")
                    binding.helpEvents.setBackgroundResource(R.drawable.vary_of_events)
                }
            }

            binding.bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.profile -> {
                        if (currentFragment !is ProfileFragment) {
                            replaceFragment(ProfileFragment())
                        }
                    }

                    R.id.map -> {
                        if (currentFragment !is MapsFragment) {
                            replaceFragment(MapsFragment())
                        }
                    }

                    R.id.rating -> {
                        if (currentFragment !is RatingFragment) {
                            replaceFragment(RatingFragment())
                        }
                    }

                    R.id.chats -> {
                        if (currentFragment !is ChatsAndGroupsFragment) {
                            replaceFragment(ChatsAndGroupsFragment())
                        }
                    }

                    R.id.reports -> {
                        if (currentFragment !is ReportsForModeratorsFragment) {
                            replaceFragment(ReportsForModeratorsFragment())
                        }
                    }

                    else -> {

                    }
                }
                true
            }
        }

    }

    private fun checkUsersRole() {
        val user = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}")
        user.child("role").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val role = snapshot.value.toString()
                    if(role == "moderator" || role == "admin") {
                        binding.bottomNavigation.menu.findItem(R.id.reports).isVisible = true
                    } else {
                        binding.bottomNavigation.menu.findItem(R.id.reports).isVisible = false
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun requestLocationPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }
        requestPermissions(permissions.toTypedArray(), LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun showOrHideMarkers(markerMap: HashMap<String, Marker>, func: String) {
        if(func != "hide")
            markerMap.forEach { it.value.isVisible = true }
        else
            markerMap.forEach { it.value.isVisible = false }
    }

    private fun resetBackgroundForBtns() {
        val type = appVM.markersType
        Log.d("INFOG", appVM.markersType)

        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val black = ContextCompat.getColor(requireContext(), R.color.inverse)
        if(type == "friends") {
            binding.friendsText.setTextColor(white)
            binding.allText.setTextColor(black)
            binding.helpText.setTextColor(black)
            binding.ecoText.setTextColor(black)
            binding.entText.setTextColor(black)
        }
        if(type == "all") {
            binding.allText.setTextColor(white)
            binding.friendsText.setTextColor(black)
            binding.helpText.setTextColor(black)
            binding.ecoText.setTextColor(black)
            binding.entText.setTextColor(black)
        }
        if(type == "eco") {
            binding.ecoText.setTextColor(white)
            binding.friendsText.setTextColor(black)
            binding.allText.setTextColor(black)
            binding.helpText.setTextColor(black)
            binding.entText.setTextColor(black)
        }
        if(type == "ent") {
            binding.entText.setTextColor(white)
            binding.friendsText.setTextColor(black)
            binding.allText.setTextColor(black)
            binding.helpText.setTextColor(black)
            binding.ecoText.setTextColor(black)
        }

        if(type == "help") {
            binding.helpText.setTextColor(white)
            binding.friendsText.setTextColor(black)
            binding.allText.setTextColor(black)
            binding.entText.setTextColor(black)
            binding.ecoText.setTextColor(black)
        }

        binding.friends.setBackgroundResource(if(type == "friends") R.drawable.vary_of_events else 0)
        binding.allEvents.setBackgroundResource(if(type == "all") R.drawable.vary_of_events else 0)
        binding.ecoEvents.setBackgroundResource(if(type == "eco") R.drawable.vary_of_events else 0)
        binding.entEvents.setBackgroundResource(if(type == "ent") R.drawable.vary_of_events else 0)
        binding.helpEvents.setBackgroundResource(if(type == "help") R.drawable.vary_of_events else 0)
    }
    private fun replaceFragment(fragment: Fragment) {
        currentFragment = fragment

        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mMap, fragment)
        fragmentTransaction.commit()
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
    override fun onDestroyView() {
        super.onDestroyView()

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

}