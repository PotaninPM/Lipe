package com.example.lipe.start_frs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.lipe.R
import com.example.lipe.databinding.FragmentStartDescThirdBinding

class StartDescThirdFragment : Fragment() {
    private var _binding: FragmentStartDescThirdBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStartDescThirdBinding.inflate(inflater, container, false)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_startDescThirdFragment_to_signUpFragment)
        }

//        binding.btnSignUp.setOnClickListener {
//            view.findNavController().navigate(R.id.action_startFragment2_to_signUpFragment)
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}