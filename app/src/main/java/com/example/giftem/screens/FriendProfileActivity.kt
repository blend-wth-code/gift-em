package com.example.giftem.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.giftem.Adapters.PostListAdapter
import com.example.giftem.R
import com.example.giftem.models.Post
import com.example.giftem.models.User
import com.google.firebase.database.FirebaseDatabase

class FriendProfileActivity : AppCompatActivity() {
    private lateinit var userId: String
    private lateinit var friendId: String
    private val database = FirebaseDatabase.getInstance()
    private lateinit var rView : RecyclerView
    private val options = mutableListOf<Post>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)
        userId = intent.getStringExtra("userId")!!
        friendId = intent.getStringExtra("friendId")!!
        rView = findViewById(R.id.post_list)
        rView.layoutManager = LinearLayoutManager(this)
        rView.adapter =  PostListAdapter(userId, options)

        findViewById<TextView>(R.id.homeMenu).setOnClickListener{handleHomeMenu()}
        findViewById<TextView>(R.id.addPostMenu).setOnClickListener{handleAddPostMenu()}
        findViewById<TextView>(R.id.profileMenu).setOnClickListener{handleProfileMenu()}
        findViewById<TextView>(R.id.logout).setOnClickListener { handleLogout() }

        database.reference.child("users/$friendId").get().addOnSuccessListener {
            snapshot ->
            val friendDetails = snapshot.getValue(User::class.java)
            val addFriendButton = findViewById<TextView>(R.id.add_friend_button)
            if (friendDetails != null) {
                findViewById<TextView>(R.id.profile_name).text =
                    "${friendDetails.fname} ${friendDetails.lname}"
                if(!friendDetails.avatar.isNullOrBlank()){
                    Glide.with(applicationContext).load(friendDetails.avatar).into(findViewById(R.id.profile_image));
                }
                if(friendDetails.approvedRequests.contains(userId)){
                    addFriendButton.visibility = View.GONE
                }
                else if(friendDetails.pendingRequests.contains(userId)){
                    addFriendButton.text = "Pending"
                    addFriendButton.isClickable = false
                    addFriendButton.visibility = View.VISIBLE
                }
                else{
                    addFriendButton.visibility = View.VISIBLE
                    addFriendButton.setOnClickListener{
                        val friendsRef = database.reference.child("users/$friendId").child("pendingRequests")
                        friendsRef.get().addOnCompleteListener {
                                snapshot ->
                            val pendingRequests = snapshot.result.value as? MutableList<String>
                            if (pendingRequests != null) {
                                pendingRequests.add(userId)
                                friendsRef.setValue(pendingRequests)
                            } else{
                                friendsRef.setValue(mutableListOf(userId))
                            }
                            Toast.makeText(this, "Friend Request Sent", Toast.LENGTH_SHORT).show()
                            addFriendButton.visibility = View.GONE
                        }
                    }
                }
            }

        }
        .addOnCompleteListener{handleMyPostsView()}
    }

    private fun handleAddPostMenu() {
        val intent = Intent(this, UploadPostActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }
    private fun handleProfileMenu() {
        val intent = Intent(this, MyProfileActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }
    private fun handleHomeMenu() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }

    private fun handleMyPostsView(){
        options.clear()
        database.reference.child("users/$friendId/posts/interests").get().addOnSuccessListener { obj ->
            obj.children.forEach { post ->
                post.getValue(Post::class.java)?.let { options.add(it) }
            }
            val adapter = PostListAdapter(userId, options)
            rView.adapter = adapter
        }
    }
    private fun handleLogout(){
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

}