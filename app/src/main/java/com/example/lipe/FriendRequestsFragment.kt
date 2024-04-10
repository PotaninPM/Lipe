package com.example.lipe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lipe.databinding.FragmentFriendRequestsBinding
import com.example.lipe.databinding.FragmentProfileBinding
import com.example.lipe.friend_requests.Request
import com.example.lipe.friend_requests.RequestsAdapter
import com.google.firebase.database.DatabaseReference

class FriendRequestsFragment : Fragment() {

    private var _binding: FragmentFriendRequestsBinding? = null
    private val binding get() = _binding!!

    private  lateinit var dbRef_user: DatabaseReference

    private val adapter = RequestsAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendRequestsBinding.inflate(inflater, container, false)

        setRequests()

        val view = binding.root
        return view
    }

    fun setRequests() {
        binding.apply {
            recyclerReq.layoutManager = LinearLayoutManager(requireContext())
            recyclerReq.adapter = adapter

            val request = Request(R.drawable.football, "de", "вв")
            adapter.addRequest(request)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}