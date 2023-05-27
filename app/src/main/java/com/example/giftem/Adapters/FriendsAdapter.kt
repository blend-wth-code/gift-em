package com.example.giftem.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.giftem.R
import com.example.giftem.models.Post
import com.example.giftem.models.User
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase

class FriendsAdapter(private val userId: String, private var friendList: MutableList<User>): RecyclerView.Adapter<FriendsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    fun setFriendList(filterFriendList: MutableList<User>){
        this.friendList = filterFriendList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = friendList[position]
        holder.name.text = "${model.fname} ${model.lname}"
        holder.remove.setOnClickListener {
            FirebaseDatabase.getInstance().reference.child("users/$userId/friends/${position}")
                .removeValue().addOnCompleteListener {
                friendList.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(holder.itemView.context, "Successfully removed", Toast.LENGTH_SHORT).show()
            }

        }
    }

    class MyViewHolder(inflater: LayoutInflater, parent: ViewGroup)
        : RecyclerView.ViewHolder(inflater.inflate(R.layout.my_friends_list, parent, false)) {
        var name: TextView = itemView.findViewById(R.id.name)
        var remove: Button = itemView.findViewById(R.id.remove)
    }

}