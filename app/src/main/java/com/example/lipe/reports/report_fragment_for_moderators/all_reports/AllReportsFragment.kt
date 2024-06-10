package com.example.lipe.reports.report_fragment_for_moderators.all_reports

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lipe.R
import com.example.lipe.Report
import com.example.lipe.chats_and_groups.all_chats.AllChatsAdapter
import com.example.lipe.chats_and_groups.all_chats.ChatItem
import com.example.lipe.databinding.FragmentAllReportsBinding
import com.example.lipe.reports.report_fragment_for_moderators.EventReport
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllReportsFragment : Fragment() {

    private lateinit var binding: FragmentAllReportsBinding

    private lateinit var adapter: AllReportsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllReportsBinding.inflate(inflater, container, false)

        adapter = AllReportsAdapter(viewLifecycleOwner.lifecycleScope)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AllReportsAdapter(viewLifecycleOwner.lifecycleScope)
        binding.reportsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.reportsRecyclerView.adapter = adapter

        setReports()
    }

    private fun setReports() {
        val dbRef_reports = FirebaseDatabase.getInstance().getReference("reports")
        dbRef_reports.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot_reports: DataSnapshot) {
                val reports_list = ArrayList<EventReport>()
                for(reports in snapshot_reports.children) {
                    val report = FirebaseDatabase.getInstance().getReference("current_events/${reports.key}")
                    report.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val imageUrl: String = snapshot.child("photos").value.toString()
                            val reports_amount: Int = snapshot_reports.child("${reports.key}").childrenCount.toString().toInt()
                            val report = EventReport(imageUrl, reports.key.toString(), reports_amount)
                            reports_list.add(report)
                            adapter.updateRequests(reports_list)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("FirebaseError","Ошибка Firebase ${error.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}