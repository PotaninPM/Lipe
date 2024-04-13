package com.example.lipe.events_in_profile

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lipe.R
import com.example.lipe.databinding.EventItemBinding
import com.example.lipe.databinding.FriendRequestItemBinding
import com.example.lipe.friend_requests.Request
import com.example.lipe.friend_requests.RequestsAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.util.ArrayList

class CurEventsAdapter : RecyclerView.Adapter<CurEventsAdapter.CurEventsHolder>() {

    val requestList = ArrayList<EventItem>()
    inner class CurEventsHolder(item: View): RecyclerView.ViewHolder(item) {
        private lateinit var dbRef: DatabaseReference

        val binding = EventItemBinding.bind(item)
        fun bind(event: EventItem) = with(binding) {
            dateTime.text = event.date_time
            title.text = event.title
            status.text = event.status
            Picasso.get().load(event.imageUrl).into(imageEvent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurEventsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
        return CurEventsHolder(view)
    }
    override fun onBindViewHolder(holder: CurEventsHolder, position: Int) {
        holder.bind(requestList[position])
    }
    override fun getItemCount(): Int {
        return requestList.size
    }

    fun removeRequest(position: Int) {
        if (position in 0 until requestList.size) {
            requestList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateRequests(events: List<EventItem>) {
        requestList.clear()
        requestList.addAll(events)
        notifyDataSetChanged()
    }
}