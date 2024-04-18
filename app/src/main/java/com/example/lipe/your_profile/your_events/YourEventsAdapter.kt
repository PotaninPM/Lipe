package com.example.lipe.your_profile.cur_events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lipe.R
import com.example.lipe.databinding.EventItemBinding
import com.example.lipe.your_profile.EventItem
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso
import java.util.ArrayList

class YourEventsAdapter : RecyclerView.Adapter<YourEventsAdapter.YourEventsHolder>() {

    val yourEvents = ArrayList<EventItem>()
    inner class YourEventsHolder(item: View): RecyclerView.ViewHolder(item) {
        private lateinit var dbRef: DatabaseReference

        val binding = EventItemBinding.bind(item)
        fun bind(event: EventItem) = with(binding) {
            dateTime.text = event.date_time
            title.text = event.title
            status.text = event.status
            Picasso.get().load(event.imageUrl).into(imageEvent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YourEventsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
        return YourEventsHolder(view)
    }
    override fun onBindViewHolder(holder: YourEventsHolder, position: Int) {
        holder.bind(yourEvents[position])
    }
    override fun getItemCount(): Int {
        return yourEvents.size
    }

    fun removeRequest(position: Int) {
        if (position in 0 until yourEvents.size) {
            yourEvents.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateRequests(events: List<EventItem>) {
        yourEvents.clear()
        yourEvents.addAll(events)
        notifyDataSetChanged()
    }
}