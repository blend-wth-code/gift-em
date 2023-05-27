package com.example.giftem.Adapters

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.giftem.R
import com.example.giftem.models.Post
import com.example.giftem.screens.ContributeActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class PostListAdapter(private val userId: String, private var posts: MutableList<Post>, private val isMyProfile:Boolean = false): RecyclerView.Adapter<PostListAdapter.MyViewHolder>() {
    private val database = FirebaseDatabase.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = posts[position]
        FirebaseDatabase.getInstance().reference.child("users/${model.createdBy}").get().addOnSuccessListener {
            snapshot ->
            holder.uname.text =
                "${snapshot.child("fname").value.toString()} ${snapshot.child("lname").value.toString()}"
        }
        holder.id.text = model.prodId
        holder.price.text = model.price.toString()
        holder.title.text = model.prodName
        if(model.prodImg.isNotBlank()){
            Glide.with(holder.post.context).load(model.prodImg).into(holder.post)
        }
        if(model.desc.isNotBlank()){
            holder.desc.text = model.desc
        }
        else holder.desc.text = ""
        val contribute = holder.itemView.findViewById<Button>(R.id.contribute)
        if(isMyProfile){
            database.reference.child("users/${model.createdBy}/posts/myContributions/await/${model.prodId}").get().addOnSuccessListener {
                if (it.value == true) {
                    contribute.text = "Checkout"
                    contribute.visibility = View.VISIBLE
                    contribute.setBackgroundColor(Color.parseColor("#2E8B57"))
                    contribute.setOnClickListener {
                        val ref = database.reference.child("users/${model.createdBy}/posts")
                        ref.child("interests/${model.prodId}").get().addOnSuccessListener { it1 ->
                            it1.ref.removeValue()
                            posts.removeIf { obj -> obj.prodId == model.prodId }
                            ref.child("myContributions/await/${model.prodId}").removeValue()
                            ref.child("myContributions/completed/${model.prodId}").setValue(true)
                            Toast.makeText(holder.itemView.context, "Successfully Purchased", Toast.LENGTH_SHORT).show()
                            setPosts(posts)
                        }
                    }
                } else {
                    contribute.visibility = View.GONE
                }
            }
        }
        else{

            database.reference.child("users/${model.createdBy}/posts/myContributions/pending/${model.prodId}").get().addOnSuccessListener {
                if(it.value == null){
                    contribute.visibility = View.VISIBLE
                    contribute.setOnClickListener {
                        val intent = Intent(holder.itemView.context, ContributeActivity::class.java)
                        intent.putExtra("userId", userId)
                        intent.putExtra("friendId", model.createdBy)
                        intent.putExtra("postId", model.prodId)
                        holder.itemView.context.startActivity(intent)
                    }
                }
                else {
                    contribute.visibility = View.GONE
                }

            }

            database.reference.child("users/${model.createdBy}/posts/myContributions/await/${model.prodId}").get().addOnSuccessListener {
                if(it.value != true){
                    contribute.visibility = View.VISIBLE
                    contribute.setOnClickListener {
                        val intent = Intent(holder.itemView.context, ContributeActivity::class.java)
                        intent.putExtra("userId", userId)
                        intent.putExtra("friendId", model.createdBy)
                        intent.putExtra("postId", model.prodId)
                        holder.itemView.context.startActivity(intent)
                    }
                }
                else {
                    contribute.visibility = View.GONE
                }

            }


        }

    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun setPosts(filterPosts: MutableList<Post>) {
        this.posts = filterPosts
        notifyDataSetChanged()
    }

    class MyViewHolder(inflater: LayoutInflater, parent: ViewGroup)
        : RecyclerView.ViewHolder(inflater.inflate(R.layout.activity_view_post, parent, false)) {
        var uname: TextView = itemView.findViewById(R.id.viewName)
        var id: TextView = itemView.findViewById(R.id.viewId)
        var post: ImageView = itemView.findViewById(R.id.viewPost)
        var price: TextView = itemView.findViewById(R.id.viewPrice)
        var desc: TextView = itemView.findViewById(R.id.viewDesc)
        var title: TextView = itemView.findViewById(R.id.viewProdName)

    }
}
