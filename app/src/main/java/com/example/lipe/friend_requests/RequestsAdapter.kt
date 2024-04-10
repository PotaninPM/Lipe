package com.example.lipe.friend_requests

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lipe.R
import com.example.lipe.databinding.FriendRequestItemBinding
import java.util.ArrayList

class RequestsAdapter: RecyclerView.Adapter<RequestsAdapter.RequestsHolder>() {

    val requestList = ArrayList<Request>()
    class RequestsHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = FriendRequestItemBinding.bind(item)
        fun bind(request: Request) = with(binding) {
            shapeableImageView.setImageResource(request.imageId)
            username.text = request.username
            desc.text = request.desc
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_request_item, parent, false)
        return RequestsHolder(view)
    }
    override fun onBindViewHolder(holder: RequestsHolder, position: Int) {
        holder.bind(requestList[position])
    }
    override fun getItemCount(): Int {
        return requestList.size
    }

    fun addRequest(request: Request) {
        requestList.add(request)
        notifyDataSetChanged()
    }


}
