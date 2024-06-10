package com.example.lipe.reports.report_fragment_for_moderators

import android.graphics.Bitmap
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class ReportsForModeratorsAdapter(val lifecycleScope: LifecycleCoroutineScope) : RecyclerView.Adapter<ReportsForModeratorsAdapter.ReportsForModeratorsHolder>() {

    val reports = ArrayList<EventReport>()
    inner class ReportsForModeratorsHolder(item: View): RecyclerView.ViewHolder(item) {

        val binding = ReportEventBinding.bind(item)
        fun bind(event: EventReport) = with(binding) {
            username.text = event.creatorUsername

            lifecycleScope.launch {
                val bitmapUser: Bitmap = withContext(Dispatchers.IO) {
                    Coil.imageLoader(itemView.context).execute(
                        ImageRequest.Builder(itemView.context)
                            .data(event.imageUserUrl)
                            .build()
                    ).drawable?.toBitmap()!!
                }
                avatar.setImageBitmap(bitmapUser)

                val bitmapEvent: Bitmap = withContext(Dispatchers.IO) {
                    Coil.imageLoader(itemView.context).execute(
                        ImageRequest.Builder(itemView.context)
                            .data(event.imageEventUrl)
                            .build()
                    ).drawable?.toBitmap()!!
                }
                eventPhoto.setImageBitmap(bitmapEvent)
            }

            binding.deleteEventBtn.setOnClickListener {

            }

            binding.onMapBtn.setOnClickListener {

            }

            binding.reportsBtn.setOnClickListener {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportsForModeratorsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.report_event, parent, false)
        return ReportsForModeratorsHolder(view)
    }
    override fun onBindViewHolder(holder: ReportsForModeratorsHolder, position: Int) {
        holder.bind(reports[position])
    }
    override fun getItemCount(): Int {
        return reports.size
    }

    fun removeRequest(position: Int) {
        if (position in 0 until reports.size) {
            reports.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateRequests(events: List<EventReport>) {
        reports.clear()
        reports.addAll(events)
        notifyDataSetChanged()
    }
}