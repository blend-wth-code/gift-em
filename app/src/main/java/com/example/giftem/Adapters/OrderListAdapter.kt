package com.example.giftem.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.giftem.R
import com.example.giftem.models.Post
import com.example.giftem.screens.MainActivity
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView

class OrderListAdapter(private val userId: String, private var posts: MutableList<Post>)
    : RecyclerView.Adapter<OrderListAdapter.MyViewHolder>() {
    private val database = FirebaseDatabase.getInstance()
    private var name: String? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = posts[position]
        holder.prodName.text = "${model.prodName}"
        holder.price.text = "${model.price}"
        if(name == null){
            database.reference.child("users/${model.createdBy}").get().addOnSuccessListener {
                name = it.child("fname").value.toString() + " " +it.child("lname").value.toString()
                holder.name.text = name
            }
        }
        else{
            holder.name.text = name
        }
        Glide.with(holder.itemView.context).load(model.prodImg).into(holder.img)
        holder.remove.setOnClickListener {
            database.reference.child("users/$userId/posts/contributions/pending/${model.createdBy}/${model.prodId}").removeValue()
            database.reference.child("users/${model.createdBy}/posts/myContributions/pending/${model.prodId}").removeValue()
            posts.removeIf { obj -> obj.prodId == model.prodId }
            if(posts.isEmpty()){
                val intent = Intent(holder.itemView.context,MainActivity::class.java)
                intent.putExtra("userId", userId)
                holder.itemView.context.startActivity(intent)
            }
            setPostsList(posts)
        }
    }

    class MyViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.activity_order_list, parent, false)) {
        var prodName: TextView = itemView.findViewById(R.id.COProdName)
        var name: TextView = itemView.findViewById(R.id.COName)
        var price: TextView = itemView.findViewById(R.id.COPrice)
        var remove: ImageButton = itemView.findViewById(R.id.CORemove)
        var img: CircleImageView = itemView.findViewById(R.id.COProdImg)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun setPostsList(filterPostList: MutableList<Post>) {
        this.posts = filterPostList;
        notifyDataSetChanged()
    }
}

