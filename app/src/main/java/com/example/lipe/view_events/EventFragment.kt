package com.example.lipe.view_events

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.example.lipe.friend_on_map_dialog.FriendOnMapBottomSheetFragment
import com.example.lipe.R
import com.example.lipe.databinding.FragmentEventBinding
import com.example.lipe.viewModels.AppVM
import com.example.lipe.viewModels.EventEcoVM
import com.example.lipe.viewModels.EventEntVM
import com.example.lipe.view_events.event_eco.EventEcoFragment
import com.example.lipe.view_events.event_ent.EventEntFragment
import com.example.lipe.view_events.event_help.EventHelpFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
        } else if(appVM.type == "help") {
            switchFragments(2)
        } else {
            switchFragments(3)
        }


    }

    private fun switchFragments(position: Int) {
        val fragment = when(position) {
            0 -> EventEntFragment()
            1 -> EventEcoFragment()
            2 -> EventHelpFragment()
            3 -> FriendOnMapBottomSheetFragment()
            else -> null
        }

        fragment?.let {
            childFragmentManager.beginTransaction()
                .replace(R.id.view_event_lay, it)
                .commit()
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.peekHeight = (resources.displayMetrics.heightPixels * 0.6).toInt()
                it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            }
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