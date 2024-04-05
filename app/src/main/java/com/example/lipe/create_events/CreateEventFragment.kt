package com.example.lipe.create_events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.example.lipe.R
import com.example.lipe.create_events.event_eco.CreateEcoEventFragment
import com.example.lipe.create_events.event_ent.CreateEntEventFragment
import com.example.lipe.create_events.event_help.CreateHelpEventFragment
import com.example.lipe.databinding.FragmentCreateEventBinding
import com.example.lipe.viewModels.AppVM
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CreateEventFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!

    private lateinit var appVM: AppVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateEventBinding.inflate(inflater, container, false)

        appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchFragmentsFields(appVM.positionCreateFr)

        binding.btnEco.setOnClickListener {
            switchFragmentsFields(0)
            appVM.positionCreateFr = 0
        }
        binding.btnEnt.setOnClickListener {
            switchFragmentsFields(1)
            appVM.positionCreateFr = 1
        }
        binding.btnHelp.setOnClickListener {
            switchFragmentsFields(2)
            appVM.positionCreateFr = 2
        }
    }

    private fun switchFragmentsFields(position: Int) {
        val fragment = when(position) {
            0 -> CreateEcoEventFragment()
            1 -> CreateEntEventFragment()
            2 -> CreateHelpEventFragment()
            else -> null
        }

        fragment?.let {
            childFragmentManager.beginTransaction()
                .replace(R.id.create_event_lay, it)
                .commit()
        }
    }

    companion object {
        fun newInstance(): CreateEventFragment {
            return CreateEventFragment()
        }

        fun show(fragmentManager: FragmentManager) {
            newInstance().show(fragmentManager, "MyBottomSheetFragment")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}