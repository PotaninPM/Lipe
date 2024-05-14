package com.example.lipe.start_frs

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.lipe.R
import com.example.lipe.databinding.FragmentStartBinding
import com.google.firebase.auth.FirebaseAuth

class StartFragment : Fragment() {

    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_startFragment2_to_signUpFragment)
        }
    }

    override fun onStart() {
        super.onStart()
        if(auth.currentUser != null) {
            view?.findNavController()?.navigate(R.id.action_startFragment2_to_mapsFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}