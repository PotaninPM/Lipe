package com.example.lipe.people_go_to_event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.databinding.PersonGoItemBinding
import com.example.lipe.databinding.RatingItemBinding
import com.example.lipe.rating_board.RatingItem
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class PeopleGoAdapter(val lifecycleScope: LifecycleCoroutineScope) :  RecyclerView.Adapter<PeopleGoAdapter.PeopleGoHolder>() {

    val peopleGoList = ArrayList<PersoneGoItem>()
    inner class PeopleGoHolder(item: View): RecyclerView.ViewHolder(item) {

        private lateinit var dbRef: DatabaseReference

        val binding = PersonGoItemBinding.bind(item)
        fun bind(person: PersoneGoItem) = with(binding) {
            lifecycleScope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    Coil.imageLoader(itemView.context).execute(
                        ImageRequest.Builder(itemView.context)
                            .data(person.image)
                            .build()
                    ).drawable?.toBitmap()
                }
                binding.persImage.setImageBitmap(bitmap)
            }
            username.text = person.username
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleGoHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.person_go_item, parent, false)
        return PeopleGoHolder(view)
    }
    override fun onBindViewHolder(holder: PeopleGoHolder, position: Int) {
        holder.bind(peopleGoList[position])
    }
    override fun getItemCount(): Int {
        return peopleGoList.size
    }
    fun removeRequest(position: Int) {
        if (position in 0 until peopleGoList.size) {
            peopleGoList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateRequests(people: List<PersoneGoItem>) {
        peopleGoList.clear()
        peopleGoList.addAll(people)
        notifyDataSetChanged()
    }
}