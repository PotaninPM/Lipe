package com.example.lipe.rating_board

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lipe.R
import com.example.lipe.databinding.RatingItemBinding
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList

class RatingAdapter :  RecyclerView.Adapter<RatingAdapter.RatingHolder>() {
    val ratingList = ArrayList<RatingItem>()
    inner class RatingHolder(item: View): RecyclerView.ViewHolder(item) {

        private lateinit var dbRef: DatabaseReference

        val binding = RatingItemBinding.bind(item)
        fun bind(rating: RatingItem) = with(binding) {
            Picasso.get().load(rating.avatarUrl).into(persImage)
            username.text = rating.username
            ratingScore.text = rating.score.toString()
            place.text = rating.place.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rating_item, parent, false)
        return RatingHolder(view)
    }
    override fun onBindViewHolder(holder: RatingHolder, position: Int) {
        holder.bind(ratingList[position])
    }
    override fun getItemCount(): Int {
        return ratingList.size
    }

    fun removeRequest(position: Int) {
        if (position in 0 until ratingList.size) {
            ratingList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateRequests(ratings: List<RatingItem>) {
        ratingList.clear()
        ratingList.addAll(ratings)
        notifyDataSetChanged()
    }
}