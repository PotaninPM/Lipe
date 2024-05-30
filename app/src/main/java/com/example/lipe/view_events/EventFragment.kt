package com.example.lipe.view_events

import android.app.Dialog
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

    private lateinit var binding: FragmentEventBinding

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
        binding = FragmentEventBinding.inflate(inflater, container, false)

        appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)

        eventEcoVM = ViewModelProvider(requireActivity()).get(EventEcoVM::class.java)

        dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")

        eventEntVM = ViewModelProvider(requireActivity()).get(EventEntVM::class.java)


        val view = binding.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
                if(appVM.type == "ent") {
                    switchFragments(0)
                } else if(appVM.type == "eco") {
                    switchFragments(1)
                } else {
                    switchFragments(0)
                }


    }

    private fun switchFragments(position: Int) {
        val fragment = when(position) {
            0 -> EventEntFragment()
            1 -> EventEcoFragment()
            else -> null
        }

        fragment?.let {
            childFragmentManager.beginTransaction()
                .replace(R.id.view_event_lay, it)
                .commit()
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = (resources.displayMetrics.heightPixels * 0.6).toInt()
        }
        return dialog
    }

    companion object {
        fun newInstance(): EventFragment {
            return EventFragment()
        }

        fun show(fragmentManager: FragmentManager) {
            newInstance().show(fragmentManager, "MyBottomSheetFragment1")
        }

    }
}