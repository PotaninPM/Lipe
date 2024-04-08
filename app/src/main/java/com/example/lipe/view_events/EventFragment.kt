package com.example.lipe.view_events

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.example.lipe.R
import com.example.lipe.databinding.FragmentEventBinding
import com.example.lipe.viewModels.AppVM
import com.example.lipe.viewModels.EventEcoVM
import com.example.lipe.viewModels.EventEntVM
import com.example.lipe.view_events.event_eco.EventEcoFragment
import com.example.lipe.view_events.event_ent.EventEntFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class EventFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!

    private  lateinit var dbRef_event: DatabaseReference

    private lateinit var appVM: AppVM

    private lateinit var eventEntVM: EventEntVM
    private lateinit var eventEcoVM: EventEcoVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventBinding.inflate(inflater, container, false)

        appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)

        eventEcoVM = ViewModelProvider(requireActivity()).get(EventEcoVM::class.java)

        dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")

        eventEntVM = ViewModelProvider(requireActivity()).get(EventEntVM::class.java)


        val view = binding.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //searchEvent(appVM.latitude, appVM.longtitude) { ready ->
            //if (ready == true) {
                if(appVM.type == "ent") {
                    switchFragments(0)
                    //Log.d("INFOG", eventEntVM.id.toString())
                } else if(appVM.type == "eco") {
                    switchFragments(1)
                } else {
                    //Log.d("INFOG", eventEntVM.id.toString())
                    switchFragments(0)
                }

        //Log.d("INFOG", appVM.event)

    }

    private fun switchFragments(position: Int) {
        val fragment = when(position) {
            0 -> EventEntFragment()
            1 -> EventEcoFragment()
            else -> null
        }

        fragment?.let {
            childFragmentManager.beginTransaction()
                .replace(R.id.create_event_lay, it)
                .commit()
        }
    }

    companion object {
        fun newInstance(): EventFragment {
            return EventFragment()
        }

        fun show(fragmentManager: FragmentManager) {
            newInstance().show(fragmentManager, "MyBottomSheetFragment1")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}