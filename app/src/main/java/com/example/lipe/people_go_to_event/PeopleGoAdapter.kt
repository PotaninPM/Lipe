package com.example.lipe.people_go_to_event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lipe.R
import java.util.ArrayList

class PeopleGoAdapter(private val usersList: ArrayList<PersoneGoItem>): RecyclerView.Adapter<PeopleGoAdapter.ViewHolderClass>() {
    class ViewHolderClass(itemView: View): RecyclerView.ViewHolder(itemView){
//        val image: ImageView = itemView.findViewById(R.id.)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
        return ViewHolderClass(view)
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        TODO("Not yet implemented")
    }
}