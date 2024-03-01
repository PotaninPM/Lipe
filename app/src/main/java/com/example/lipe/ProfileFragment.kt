package com.example.lipe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.lipe.databinding.FragmentMapsBinding
import com.example.lipe.databinding.FragmentProfileBinding
import com.google.android.gms.maps.SupportMapFragment

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchTabs(0)
//        binding.bottomNavigation.setOnItemSelectedListener {
//            when(it.itemId) {
//                R.id.map -> view.findNavController().navigate(R.id.action_profileFragment_to_mapsFragment)
//
//                else -> {
//
//                }
//            }
//            true
//        }


    }
    private fun switchTabs(position: Int) {
        val fragment = when(position) {
            0 -> EventsInProfileFragment()
            1 -> RatingFragment()
            2 -> SignUpFragment()
            else -> null
        }

        fragment?.let {
            childFragmentManager.beginTransaction()
                .replace(R.id.switcher, it)
                .commit()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}