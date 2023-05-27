package com.example.giftem.screens

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.giftem.Adapters.PostListAdapter
import com.example.giftem.Adapters.SearchAdapter
import com.example.giftem.R
import com.example.giftem.models.Post
import com.example.giftem.models.User
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private var adapter: PostListAdapter? = null
    private var searchAdapter: SearchAdapter? = null
    private lateinit var userId: String
    private val friendsList = mutableListOf<User>()
    private val options = mutableListOf<Post>()
    private val database = FirebaseDatabase.getInstance()
    private lateinit var rView: RecyclerView
    private val searchHandler = Handler()
    private var searchRunnable: Runnable? = null
    private lateinit var noPosts: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        noPosts = findViewById(R.id.noData)
        userId =  intent.getStringExtra("userId")!!
        findViewById<TextView>(R.id.addPostMenu).setOnClickListener { handlePostMenu() }
        findViewById<TextView>(R.id.profileMenu).setOnClickListener { handleProfileMenu() }
        findViewById<TextView>(R.id.logout).setOnClickListener { handleLogout() }
        val search = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchPost)
        search.clearFocus()
        options.clear()


        rView = findViewById(R.id.rView)
        rView.layoutManager = LinearLayoutManager(this)
        adapter = PostListAdapter(userId, options)
        rView.adapter = adapter


        handleDisplayPosts()
        search.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Perform search when user hits the search button
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                // Cancel any previous searchRunnable if it exists
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                // Create a new searchRunnable to execute after a delay
                searchRunnable = Runnable {
                    getFriends(newText)
                }
                // Schedule the searchRunnable to execute after 500 milliseconds
                searchHandler.postDelayed(searchRunnable!!, 500)
                return true
            }
        })
    }

    private fun handlePostMenu() {
        val intent = Intent(this, UploadPostActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }

    private fun handleProfileMenu() {
        val intent = Intent(this, MyProfileActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }

    private fun handleLogout(){
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun getFriends(newText: String?) {
        if (newText.isNullOrBlank()){
            friendsList.clear()
            searchAdapter?.setUser(friendsList)
            handleDisplayPosts()
            return
        }
        friendsList.clear()
        searchAdapter = SearchAdapter(userId, friendsList)
        rView.adapter = searchAdapter
        database.reference.child("users").get().addOnSuccessListener {
            friend -> friend.children.forEach { profile ->
                if(profile != null && profile.value != null){
                    val curFriend: User = profile.getValue(User::class.java)!!
                    if(curFriend.id != userId
                        && (curFriend.lname.contains(Regex(newText, RegexOption.IGNORE_CASE))
                                || curFriend.fname.contains(Regex(newText, RegexOption.IGNORE_CASE)))) {
                        friendsList.add(curFriend)
                    }
                }
            }
            if(friendsList.isNotEmpty()){
                searchAdapter?.setUser(friendsList)
                rView.visibility = View.VISIBLE
            }
            noPosts.visibility = View.GONE
        }
    }

    private fun handleDisplayPosts(){
        noPosts.visibility = View.VISIBLE
        database.reference.child("users/$userId/friends").get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { friend ->
                if(friend.value.toString().isNotBlank()){
                    database.reference.child("users/${friend.value}/posts/interests").get().addOnSuccessListener { obj ->
                        var index = obj.childrenCount
                        obj.children.forEach { post ->
                            index--
                            val postObj = post.getValue(Post::class.java)!!
                            val ref = database.reference.child("users/${userId}/posts/contributions/pending/${postObj.prodId}")
                            ref.get().addOnSuccessListener {
                                if(it.value == null){
                                    options.add(postObj)
                                }
                                if(index <= 0 && options.isNotEmpty()){
                                    rView.visibility = View.VISIBLE
                                    noPosts.visibility = View.GONE
                                    adapter?.setPosts(options)
                                }
                            }.addOnCompleteListener {
                                if (options.isNotEmpty()) {
                                    rView.visibility = View.VISIBLE
                                    noPosts.visibility = View.GONE
                                    adapter = PostListAdapter(userId, options)
                                    rView.adapter = adapter
                                } else {
                                    rView.visibility = View.GONE
                                    noPosts.visibility = View.VISIBLE
                                }
                            }

                        }
                    }
                }
            }
        }
    }

}

