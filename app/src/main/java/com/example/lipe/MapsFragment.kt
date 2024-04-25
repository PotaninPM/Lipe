package com.example.lipe

import ProfileFragment
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
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
class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null

    //View Model
    private lateinit var appVM: AppVM

    private val eventEntVM: EventEntVM by activityViewModels()

    private lateinit var eventEcoVM: EventEcoVM

    private lateinit var saveStateMapVM: SaveStateMapsVM

    val markers: HashMap<String, Marker> = HashMap()

    private lateinit var locationManager: LocationManager

    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap

    private lateinit var dbRef_user: DatabaseReference
    private lateinit var dbRef_event: DatabaseReference

    val eventsMarkersMap = HashMap<String, Marker>()

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")

        // show all markers on map
        dbRef_event.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val coordinates: List<Double>? = dataSnapshot.child("coordinates").getValue(object : GenericTypeIndicator<List<Double>>() {})
                val eventId: String = dataSnapshot.child("event_id").value.toString()
                val type = dataSnapshot.child("type_of_event").value?.toString() ?: "def_type"
                if (coordinates != null) {
                    val marker = addMarker(LatLng(coordinates[0], coordinates[1]), type, eventId)
                    eventsMarkersMap[eventId] = marker!!
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val eventId: String = dataSnapshot.child("event_id").value.toString()
                eventsMarkersMap[eventId]?.remove()
                eventsMarkersMap.remove(eventId)
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
                    Log.d("INFOG", appVM.type)
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


    private fun addMarker(latLng: LatLng, type: String, eventId: String): Marker? {
        var marker: Marker? = null
        if(type == "ent") {
            val markerLayout = LayoutInflater.from(requireContext()).inflate(R.layout.custom_marker, null)
            val markerImageView = markerLayout.findViewById<ImageView>(R.id.imageView)

            if(true) {
                markerImageView.setImageResource(R.drawable.football)
            }

            val markerOptions = MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(markerLayout)))
                .anchor(0.5f, 1f)

            marker = mMap.addMarker(markerOptions)

        } else if(type == "eco") {
            val markerLayout = LayoutInflater.from(requireContext()).inflate(R.layout.custom_marker, null)
            val markerImageView = markerLayout.findViewById<ImageView>(R.id.imageView)

            if(true) {
                markerImageView.setImageResource(R.drawable.leaf)
            }

            val markerOptions = MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(markerLayout)))
                .anchor(0.5f, 1f)

            marker = mMap.addMarker(markerOptions)
        }
        return marker
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)
//        eventEntVM = ViewModelProvider(requireActivity()).get(EventEntVM::class.java)
        eventEcoVM = ViewModelProvider(requireActivity()).get(EventEcoVM::class.java)
        saveStateMapVM = ViewModelProvider(requireActivity()).get(SaveStateMapsVM::class.java)

//        binding.entEvents.setOnClickListener {
//            showMarkersByType(listOf("ent"))
//        }
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)


        binding.allEvents.setBackgroundResource(R.drawable.vary_of_events)


        if(appVM.reg == "yes") {
            showSuccessRegWindow()
            appVM.reg = "no"
        }

        binding.friends.setOnClickListener {
            if (appVM.markersType != "friends") {
                resetMarkers()
                appVM.markersType = "friends"
                binding.friends.setBackgroundResource(R.drawable.vary_of_events)
            }
        }

        binding.allEvents.setOnClickListener {
            if (appVM.markersType != "all") {
                resetMarkers()
                appVM.markersType = "all"
                binding.allEvents.setBackgroundResource(R.drawable.vary_of_events)
            }
        }

        binding.ecoEvents.setOnClickListener {
            if (appVM.markersType != "eco") {
                resetMarkers()
                appVM.markersType = "eco"
                binding.ecoEvents.setBackgroundResource(R.drawable.vary_of_events)
            }
        }

        binding.entEvents.setOnClickListener {
            if (appVM.markersType != "ent") {
                resetMarkers()
                appVM.markersType = "ent"
                binding.entEvents.setBackgroundResource(R.drawable.vary_of_events)
            }
        }

        binding.helpEvents.setOnClickListener {
            if (appVM.markersType != "help") {
                resetMarkers()
                appVM.markersType = "help"
                binding.helpEvents.setBackgroundResource(R.drawable.vary_of_events)
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
    private fun resetMarkers() {
        binding.friends.setBackgroundResource(0)
        binding.allEvents.setBackgroundResource(0)
        binding.ecoEvents.setBackgroundResource(0)
        binding.entEvents.setBackgroundResource(0)
        binding.helpEvents.setBackgroundResource(0)

        eventsMarkersMap.clear()
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
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
