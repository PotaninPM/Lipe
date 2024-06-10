package com.example.lipe.reports.report_fragment_for_moderators.all_reports

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.databinding.ReportEventBinding
import com.example.lipe.reports.report_fragment_for_moderators.EventReport
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class AllReportsAdapter(val lifecycleScope: LifecycleCoroutineScope) : RecyclerView.Adapter<AllReportsAdapter.AllReportsHolder>() {

    val reports_list = ArrayList<EventReport>()
    inner class AllReportsHolder(item: View): RecyclerView.ViewHolder(item) {

        val binding = ReportEventBinding.bind(item)
        fun bind(event: EventReport) = with(binding) {

            lifecycleScope.launch {
                val bitmapEvent: Bitmap = withContext(Dispatchers.IO) {
                    Coil.imageLoader(itemView.context).execute(
                        ImageRequest.Builder(itemView.context)
                            .data(event.imageEventUrl)
                            .build()
                    ).drawable?.toBitmap()!!
                }
                eventPhoto.setImageBitmap(bitmapEvent)
            }

            binding.reportsBtn.setText(binding.reportsBtn.text.toString() + " ${event.reports}")

            binding.deleteEventBtn.setOnClickListener {
                val dbRef_event = FirebaseDatabase.getInstance().getReference("current_events/${event.eventUid}/reg_people_id")
                var users_list = arrayListOf<String>()
                dbRef_event.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(users in snapshot.children) {
                            users_list.add(users.value.toString())
                        }
                        deleteEvent(event.eventUid, users_list)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
                //deleteEvent(event.eventUid, users_list)
            }

            binding.onMapBtn.setOnClickListener {

            }

            binding.reportsBtn.setOnClickListener {

            }
        }
    }

    private fun deleteEvent(uid: String, users: ArrayList<String>) {
        val dbRef_user = FirebaseDatabase.getInstance().getReference("users")
        val curPeople = FirebaseDatabase.getInstance().getReference("current_events").child(uid)
        val dbRef_group = FirebaseDatabase.getInstance().getReference("groups").child(uid)
        val reports = FirebaseDatabase.getInstance().getReference("reports/${uid}")

        for(user in users) {
            dbRef_user.child(user).child("curRegEventsId").child(uid).removeValue().addOnSuccessListener {

            }
        }
        curPeople.removeValue().addOnSuccessListener {
            dbRef_group.removeValue().addOnSuccessListener {
                reports.removeValue().addOnSuccessListener {
    //                val eventFragment = parentFragment as? EventFragment
    //                eventFragment?.dismiss()
    //
    //                binding.deleteOrLeave.visibility = View.GONE
    //                binding.btnRegToEvent.visibility = View.VISIBLE
                    }
                }
            }
            .addOnFailureListener {
                Log.e("INFOG", "ErLeaveEvent")
            }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllReportsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.report_event, parent, false)
        return AllReportsHolder(view)
    }
    override fun onBindViewHolder(holder: AllReportsHolder, position: Int) {
        holder.bind(reports_list[position])
    }
    override fun getItemCount(): Int {
        return reports_list.size
    }

    fun removeRequest(position: Int) {
        if (position in 0 until reports_list.size) {
            reports_list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateRequests(events: List<EventReport>) {
        reports_list.clear()
        reports_list.addAll(events)
        notifyDataSetChanged()
    }
}