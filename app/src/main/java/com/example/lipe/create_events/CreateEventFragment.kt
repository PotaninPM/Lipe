package com.example.lipe.create_events

import android.content.res.ColorStateList
import android.graphics.Color
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
import com.google.android.material.button.MaterialButton

class CreateEventFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentCreateEventBinding

    private lateinit var appVM: AppVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false)

        appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(isAdded) {
            switchFragmentsFields(appVM.positionCreateFr)

            binding.buttonEco.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#00AE1C"))

            binding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
                val buttons = arrayOf(
                    R.id.buttonEco,
                    R.id.buttonEnt,
                    R.id.buttonHelp
                )

                if (isChecked) {
                    group.findViewById<MaterialButton>(checkedId).backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#00AE1C"))
                    group.findViewById<MaterialButton>(checkedId).isChecked = true

                    buttons.forEach { buttonId ->
                        if (buttonId != checkedId) {
                            group.findViewById<MaterialButton>(buttonId).backgroundTintList =
                                ColorStateList.valueOf(Color.TRANSPARENT)
                            group.findViewById<MaterialButton>(buttonId).isChecked = false
                        }
                    }

                    when (checkedId) {
                        R.id.buttonEco -> {
                            switchFragmentsFields(0)
                            appVM.positionCreateFr = 0
                        }

                        R.id.buttonEnt -> {
                            switchFragmentsFields(1)
                            appVM.positionCreateFr = 1
                        }

                        R.id.buttonHelp -> {
                            switchFragmentsFields(2)
                            appVM.positionCreateFr = 2
                        }
                    }
                }
            }
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
}