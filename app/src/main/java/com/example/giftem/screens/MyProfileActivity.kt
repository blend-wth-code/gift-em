package com.example.giftem.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.giftem.Adapters.ApprovedFriendsAdapter
import com.example.giftem.Adapters.FriendRequestAdapter
import com.example.giftem.Adapters.FriendsAdapter
import com.example.giftem.Adapters.PostListAdapter
import com.example.giftem.R
import com.example.giftem.models.Post
import com.example.giftem.models.User
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class MyProfileActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private lateinit var userId: String
    private lateinit var rView : RecyclerView

    private var imageUri: Uri? = null
    private var imageView: ImageView? = null
    private var imageData: ByteArray? = null
    private lateinit var noData: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        noData = findViewById(R.id.noContent)
        userId = intent.getStringExtra("userId")!!
        rView = findViewById(R.id.rProfileVIew)
        findViewById<ImageView>(R.id.avatar).setOnClickListener{handlePick()}
        database.reference.child("users/$userId").get().addOnSuccessListener{
            snapshot ->
            val name = snapshot.child("fname").value.toString() + " " + snapshot.child("lname").value.toString()
            val avatar = snapshot.child("avatar").value.toString()
            findViewById<TextView>(R.id.uname).text = name
            if(avatar.isNotBlank()){
                Glide.with(applicationContext).load(avatar).into(findViewById<CircleImageView>(R.id.avatar));
            }
        }
        handleMyPostsView()

        findViewById<Button>(R.id.posts).setOnClickListener{handleMyPostsView()}
        findViewById<Button>(R.id.pending_friends).setOnClickListener{handlePendingFriendsView()}
        findViewById<Button>(R.id.approved_friends).setOnClickListener{handleApprovedFriendsView()}
        findViewById<Button>(R.id.friends).setOnClickListener{handleFriendsView()}

        findViewById<TextView>(R.id.homeMenu).setOnClickListener{handleHomeMenu()}
        findViewById<TextView>(R.id.addPostMenu).setOnClickListener{handleAddPostMenu()}
        findViewById<TextView>(R.id.logout).setOnClickListener { handleLogout() }

    }

    private fun handleAddPostMenu() {
        val intent = Intent(this, UploadPostActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }

    private fun handleHomeMenu() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }
    private fun handleLogout(){
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun handleFriendsView() {
        val options = mutableListOf<User>()
        val adapter = FriendsAdapter(userId, options)
        rView.adapter = adapter
        rView.layoutManager = LinearLayoutManager(this)

        database.reference.child("users/$userId/friends").get().addOnSuccessListener { snapshot ->
            var index = snapshot.childrenCount
            if(index <= 0){
                noData.text = "No Followers to display"
                noData.visibility = View.VISIBLE
            }
            snapshot.children.forEach { id ->
                database.reference.child("users/${id.value}").get().addOnSuccessListener { user ->
                    val userDetails = user.getValue(User::class.java)
                    if (userDetails != null) {
                        options.add(userDetails)
                    }
                    index--
                    if (index <= 0) {
                        if(options.isEmpty()){
                            noData.text = "No Pending Requests to display"
                            noData.visibility = View.VISIBLE
                        }
                        else{
                            noData.visibility = View.GONE
                            adapter.setFriendList(options)
                        }
                    }
                }
            }
        }
    }

    private fun handlePendingFriendsView() {
        val options = mutableListOf<User>()
        val adapter = FriendRequestAdapter(userId, options)
        rView.adapter = adapter
        rView.layoutManager = LinearLayoutManager(this)

        database.reference.child("users/$userId/pendingRequests").get().addOnSuccessListener { snapshot ->
            var index = snapshot.childrenCount
            if(index <= 0){
                noData.text = "No Pending Requests to display"
                noData.visibility = View.VISIBLE
            }
            snapshot.children.forEach { id ->
                database.reference.child("users/${id.value}").get().addOnSuccessListener { user ->
                    val userDetails = user.getValue(User::class.java)
                    if (userDetails != null) {
                        options.add(userDetails)
                    }
                    index--
                    if (index <= 0) {
                        if(options.isEmpty()){
                            noData.text = "No Pending Requests to display"
                            noData.visibility = View.VISIBLE
                        }
                        else{
                            noData.visibility = View.GONE
                            adapter.setFriendList(options)
                        }
                    }
                }
            }
        }
    }

    private fun handleApprovedFriendsView() {
        val options = mutableListOf<User>()
        val adapter = ApprovedFriendsAdapter(userId, options)
        rView.adapter = adapter
        rView.layoutManager = LinearLayoutManager(this)

        database.reference.child("users/$userId/approvedRequests").get().addOnSuccessListener { snapshot ->
            var index = snapshot.childrenCount
            if(index <= 0){
                noData.text = "No Approved Requests to display"
                noData.visibility = View.VISIBLE
            }
            snapshot.children.forEach { id ->
                database.reference.child("users/${id.value}").get().addOnSuccessListener { user ->
                    val userDetails = user.getValue(User::class.java)
                    if (userDetails != null) {
                        options.add(userDetails)
                    }
                    index--
                    if (index <= 0) {
                        if(options.isEmpty()){
                            noData.text = "No Approved Requests to display"
                            noData.visibility = View.VISIBLE
                        }
                        else{
                            noData.visibility = View.GONE
                            adapter.setFriendList(options)
                        }
                    }
                }
            }
        }
    }

    private fun handleMyPostsView(){
        val options = mutableListOf<Post>()
        val adapter = PostListAdapter(userId, options, true)
        rView.layoutManager = LinearLayoutManager(this)
        rView.adapter = adapter
        database.reference.child("users/$userId/posts/interests").get().addOnSuccessListener { obj ->
            obj.children.forEach { post ->
                post.getValue(Post::class.java)?.let { options.add(it) }
            }
            if(options.isEmpty()){
                noData.text = "Add items to your wishlist"
                noData.visibility = View.VISIBLE
            }
            else{
                noData.visibility = View.GONE
                options.reverse()
                adapter.setPosts(options)
            }
        }
    }



    private fun handlePick() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), 1)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            try {
                val imageBitMap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                imageView?.setImageBitmap(imageBitMap)
                val boas = ByteArrayOutputStream()
                imageBitMap.compress(Bitmap.CompressFormat.PNG, 100, boas)
                imageData = boas.toByteArray()
                Glide.with(this)
                    .load(imageUri.toString())
                    .into(findViewById(R.id.avatar))
                handleUpload()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun handleUpload() {
        val fileName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("users/${userId}/avatar/${fileName}")
        val uploadTask = ref.putBytes(imageData!!)
        uploadTask.addOnSuccessListener {uri ->
            uri.storage.downloadUrl
                .addOnCompleteListener {img ->
                    val postRef = FirebaseDatabase.getInstance().getReference("users/$userId/avatar")
                    postRef.setValue(img.result.toString())
                    Toast.makeText(this, "Image upload successful", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload successful but download URL retrieval failed.", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Toast.makeText(this, "Image upload failed.", Toast.LENGTH_SHORT).show()
        }
    }
}