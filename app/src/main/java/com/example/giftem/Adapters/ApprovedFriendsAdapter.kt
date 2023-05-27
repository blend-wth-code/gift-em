package com.example.giftem.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.giftem.R
import com.example.giftem.models.User
import com.google.firebase.database.FirebaseDatabase

class ApprovedFriendsAdapter(private val userId: String, private var friendList: MutableList<User>)
    : RecyclerView.Adapter<ApprovedFriendsAdapter.MyViewHolder>() {
    private  val database = FirebaseDatabase.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = friendList[position]
        holder.name.text = "${model.fname} ${model.lname}"
        holder.remove.setOnClickListener {
            val ref = database.reference.child("users/$userId/approvedRequests")
            ref.get().addOnSuccessListener {
                snapshot -> snapshot.children.forEach {
                    if(it.value == model.id){
                        ref.child("${it.key}").removeValue()
                        friendList.removeIf { user -> user.id == model.id }
                        notifyDataSetChanged()
                        Toast.makeText(holder.itemView.context, "Successfully removed", Toast.LENGTH_SHORT).show()
                    }
                }
                val friendsRef = database.reference.child("users/${model.id}/friends")
                friendsRef.get().addOnSuccessListener {
                        friend -> friend.children.forEach {
                        if(it.value == model.id){
                            ref.child("${it.key}").removeValue()
                        }
                    }
                }
            }


        }
    }

    class MyViewHolder(inflater: LayoutInflater, parent: ViewGroup)
        : RecyclerView.ViewHolder(inflater.inflate(R.layout.my_friends_list, parent, false)) {
        var name: TextView = itemView.findViewById(R.id.name)
        var remove: Button = itemView.findViewById(R.id.remove)
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    fun setFriendList(filterFriendList: MutableList<User>){
        this.friendList = filterFriendList;
        notifyDataSetChanged()
    }

}