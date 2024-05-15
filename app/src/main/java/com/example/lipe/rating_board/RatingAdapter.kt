package com.example.lipe.rating_board

import android.text.TextUtils.replace
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.all_profiles.other_profile.OtherProfileFragment
import com.example.lipe.databinding.RatingItemBinding
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList

class RatingAdapter(val lifecycleScope: LifecycleCoroutineScope) :  RecyclerView.Adapter<RatingAdapter.RatingHolder>() {

    val ratingList = ArrayList<RatingItem>()
    inner class RatingHolder(item: View): RecyclerView.ViewHolder(item) {

        private lateinit var dbRef: DatabaseReference

        val binding = RatingItemBinding.bind(item)
        fun bind(rating: RatingItem) = with(binding) {
            lifecycleScope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    Coil.imageLoader(itemView.context).execute(
                        ImageRequest.Builder(itemView.context)
                            .data(rating.avatarUrl)
                            .build()
                    ).drawable?.toBitmap()
                }
                binding.persImage.setImageBitmap(bitmap)
            }
            username.text = rating.username
            ratingScore.text = rating.score.toString()
            place.text = rating.place.toString()

            val backgroundResId = when (rating.place) {
                1 -> R.drawable.rating_item_source_first
                2 -> R.drawable.rating_item_source_second
                3 -> R.drawable.rating_item_source_third
                else -> R.drawable.rating_item_source
            }
            ratingItem.setBackgroundResource(backgroundResId)

            binding.ratingItem.setOnClickListener {
//                val action = RatingFragmentDirections.actionRatingFragmentToOtherProfileFragment(rating.uid)
//                it.findNavController().navigate(action)
            }
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
    fun filter(text: String) {
        val filteredList = ArrayList<RatingItem>()
        for (item in ratingList) {
            if (item.username.contains(text, ignoreCase = true)) {
                filteredList.add(item)
            }
        }
        updateRequests(filteredList)
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