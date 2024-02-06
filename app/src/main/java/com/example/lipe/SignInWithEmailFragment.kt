package com.example.lipe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.lipe.databinding.FragmentSignInWithEmailBinding
import com.example.lipe.databinding.FragmentSignUpBinding
import com.example.lipe.databinding.FragmentStartBinding

class SignInWithEmailFragment : Fragment() {

    private var _binding: FragmentSignInWithEmailBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignInWithEmailBinding.inflate(inflater, container, false)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.btnSignIn.setOnClickListener {
//            view.findNavController().navigate(R.id.action_signInWithEmailFragment_to_mapsFragment)
//        }

//        binding.txtNoAc.setOnClickListener {
//            view.findNavController().navigate(R.id.action_signInWithEmailFragment_to_signUpFragment)
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}