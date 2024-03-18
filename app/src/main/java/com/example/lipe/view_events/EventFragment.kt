package com.example.lipe.view_events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.example.lipe.R
import com.example.lipe.databinding.FragmentEventBinding
import com.example.lipe.view_events.event_ent.EventEntFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EventFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventBinding.inflate(inflater, container, false)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchFragments(0)

    }

    private fun switchFragments(position: Int) {
        val fragment = when(position) {
            0 -> EventEntFragment()
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