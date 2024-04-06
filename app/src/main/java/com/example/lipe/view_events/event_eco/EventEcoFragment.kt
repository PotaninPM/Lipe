package com.example.lipe.view_events.event_eco

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lipe.R
import com.example.lipe.databinding.FragmentEventEcoBinding
import com.example.lipe.databinding.FragmentProfileBinding
import com.example.lipe.viewModels.AppVM
import com.example.lipe.viewModels.EventEcoVM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference

class EventEcoFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private lateinit var dbRef: DatabaseReference

    private lateinit var dbRef_event: DatabaseReference

    private lateinit var dbRef_user: DatabaseReference

    private lateinit var storageRef : StorageReference

    private lateinit var appVM: AppVM

    private var _binding: FragmentEventEcoBinding? = null
    private val binding get() = _binding!!

    private val eventEcoVM: EventEcoVM by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEventEcoBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = eventEcoVM
        }

        auth = FirebaseAuth.getInstance()
        appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.allEntEvent.visibility = View.GONE
//        binding.loadingProgressBar.visibility = View.VISIBLE
    }

}