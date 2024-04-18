package com.example.lipe.rating_board

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lipe.databinding.FragmentRatingBinding
import com.example.lipe.friend_requests.RequestsAdapter

class RatingFragment : Fragment() {

    private var _binding: FragmentRatingBinding? = null
    private val binding get() = _binding!!

    private var rateList = ArrayList<RatingItem>()
    private lateinit var adapter: RatingAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RatingAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.recyclerView.setHasFixedSize(true)

        addPeople()
//        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                filterList(newText)
//                return true
//            }
//
//        })
    }

    private fun filterList(query: String) {
        if(query != null) {
            val filteredList = ArrayList<RatingItem>()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRatingBinding.inflate(inflater, container, false)

        val view = binding.root
        return view
    }

    private fun addPeople() {
        //val dbRef_user = FirebaseDatabase.getInstance().getReference("rating/${auth.currentUser!!.uid}/query_friends")
        rateList.add(RatingItem("1", "https://s0.rbk.ru/v6_top_pics/media/img/0/95/346980502063950.webp", 1, "MikePM", 123))
        rateList.add(RatingItem("1", "https://s0.rbk.ru/v6_top_pics/media/img/0/95/346980502063950.webp", 1, "MikePM", 123))
        rateList.add(RatingItem("1", "https://s0.rbk.ru/v6_top_pics/media/img/0/95/346980502063950.webp", 1, "MikePM", 123))

        adapter.updateRequests(rateList)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}