package com.example.lipe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import com.example.lipe.databinding.FragmentCreateEventBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout

class CreateEventFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateEventBinding.inflate(inflater, container, false)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchFragmentsFields(0)


        binding.close.setOnClickListener {
            dismiss()
        }

        binding.btnEnt.setBackgroundColor(getResources().getColor(R.color.invisible))
        binding.btnEnt.setTextColor(getResources().getColor(R.color.black))

        binding.btnHelp.setBackgroundColor(getResources().getColor(R.color.invisible))
        binding.btnHelp.setTextColor(getResources().getColor(R.color.black))

        binding.btnEco.setOnClickListener {
            switchFragmentsFields(0)


            binding.btnEnt.setBackgroundColor(getResources().getColor(R.color.invisible))
            binding.btnEnt.setTextColor(getResources().getColor(R.color.black))

            binding.btnHelp.setBackgroundColor(getResources().getColor(R.color.invisible))
            binding.btnHelp.setTextColor(getResources().getColor(R.color.black))

            binding.btnEco.setTextColor(getResources().getColor(R.color.white))
            binding.btnEco.setBackgroundResource(R.drawable.create_event_change_type_btn)

        }
        binding.btnEnt.setOnClickListener {
            switchFragmentsFields(1)

            binding.btnEco.setBackgroundColor(getResources().getColor(R.color.invisible))
            binding.btnEco.setTextColor(getResources().getColor(R.color.black))

            binding.btnHelp.setBackgroundColor(getResources().getColor(R.color.invisible))
            binding.btnHelp.setTextColor(getResources().getColor(R.color.black))

            binding.btnEnt.setTextColor(getResources().getColor(R.color.white))
            binding.btnEnt.setBackgroundResource(R.drawable.create_event_change_type_btn)
        }
        binding.btnHelp.setOnClickListener {
            switchFragmentsFields(2)

            binding.btnEco.setBackgroundColor(getResources().getColor(R.color.invisible))
            binding.btnEco.setTextColor(getResources().getColor(R.color.black))

            binding.btnEnt.setBackgroundColor(getResources().getColor(R.color.invisible))
            binding.btnEnt.setTextColor(getResources().getColor(R.color.black))

            binding.btnHelp.setTextColor(getResources().getColor(R.color.white))
            binding.btnHelp.setBackgroundResource(R.drawable.create_event_change_type_btn)
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