package com.example.giftem.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.giftem.R
import com.example.giftem.models.User
import com.example.giftem.screens.FriendProfileActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SearchAdapter(private var userId: String, private var userList: List<User>): RecyclerView.Adapter<SearchAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = userList[position]
        holder.name.text = model.fname + " " + model.lname
        holder.name.setOnClickListener{
            val intent = Intent(holder.itemView.context, FriendProfileActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("friendId", model.id)
            holder.itemView.context.startActivity(intent)
        }
//        val storeRef: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(model.prodImg)
//        Glide.with(holder.post.context).load(storeRef).into(holder.post)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun setUser(filterUserList: List<User>) {
        this.userList = filterUserList
        notifyDataSetChanged()
    }

    class MyViewHolder(inflater: LayoutInflater, parent: ViewGroup)
        : RecyclerView.ViewHolder(inflater.inflate(R.layout.search_list, parent, false)) {
        var name: TextView = itemView.findViewById(R.id.name)
    }
}
