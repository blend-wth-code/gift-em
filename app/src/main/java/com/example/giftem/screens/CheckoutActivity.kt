package com.example.giftem.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.giftem.Adapters.OrderListAdapter
import com.example.giftem.R
import com.example.giftem.models.Post
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CheckoutActivity : AppCompatActivity() {
    private lateinit var userId:String
    private var postId:String? = null
    private lateinit var friendId:String
    private val database = FirebaseDatabase.getInstance()
    private lateinit var rView : RecyclerView
    private val options = mutableListOf<Post>()
    private lateinit var adapter: OrderListAdapter
    private lateinit var friendRef:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        userId = intent.getStringExtra("userId")!!
        postId = intent.getStringExtra("postId")
        friendId = intent.getStringExtra("friendId")!!
        friendRef = database.reference.child("users/$friendId/posts")
        findViewById<TextView>(R.id.homeMenu).setOnClickListener{handleHomeMenu()}
        findViewById<TextView>(R.id.addPostMenu).setOnClickListener{handleAddPostMenu()}
        findViewById<TextView>(R.id.profileMenu).setOnClickListener{handleProfileMenu()}
        findViewById<TextView>(R.id.logout).setOnClickListener { handleLogout() }
        rView = findViewById(R.id.CORView)
        rView.layoutManager = LinearLayoutManager(this)
        adapter = OrderListAdapter(userId, options)
        rView.adapter = adapter
        handleDisplayPosts()
        findViewById<Button>(R.id.pay).setOnClickListener{handleCheckout()}
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

    private fun handleLogout(){
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun handleCheckout(){
        val fname = findViewById<EditText>(R.id.first_name_edit_text)
        var isValid = true
        if (fname.text.isNullOrBlank()){
            isValid = false
            fname.error ="First Name is required"
        }

        val lname = findViewById<EditText>(R.id.last_name_edit_text)
        if (lname.text.isNullOrBlank()){
            isValid = false
            lname.error ="Last Name is required"
        }

        val card = findViewById<EditText>(R.id.card_number_edit_text)
        if (card.text.isNullOrBlank()){
            isValid = false
            card.error ="Card Number is required"
        }
        else if (!"^\\d{4}\\d{4}\\d{4}\\d{4}\$".toRegex().matches(card.text.toString())) {
            isValid = false
            card.error = "Invalid Card Number format(################)"
        }

        val cvv = findViewById<EditText>(R.id.cvv_edit_text)
        if (cvv.text.isNullOrBlank()){
            isValid = false
            cvv.error ="CVV is required"
        }
        else if (!"^\\d{3}\$".toRegex().matches(cvv.text.toString())) {
            isValid = false
            cvv.error = "Invalid CVV format(###)"
        }

        val date = findViewById<EditText>(R.id.expiry_edit_text)
        if (date.text.isNullOrBlank()){
            date.error ="Expiry Date is required"
            return
        }
        try {
            val today = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("MM/yy", Locale.US)
            val enteredDate = dateFormat.parse(date.text.toString())
            if (enteredDate.before(today)) {
                date.error = "Invalid Expiry Date(MM/yy)"
                return
            }
        } catch (e: ParseException) {
            date.error = "Invalid Expiry Date(MM/yy)"
            return
        }

        if(!isValid) return
        val myRef = database.reference.child("users/$userId/posts/contributions")
        myRef.child("pending/$friendId").get().addOnSuccessListener {
            snapshot -> snapshot.children.forEach {
                it.ref.removeValue()
                friendRef.child("myContributions/await/${it.key}").setValue(true)
                friendRef.child("myContributions/pending/${it.key}").removeValue()
                myRef.child("completed/$friendId/${it.key}").setValue(true)
                Toast.makeText(this, "Contribution Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
        }
    }
    private fun handleDisplayPosts(){
        options.clear()
        if(postId != null){
            friendRef.child("interests/$postId").get().addOnSuccessListener {
                val posts = it.getValue(Post::class.java)!!
                options.add(posts)
                adapter.setPostsList(options)
            }
        }
        else{
            database.reference.child("users/$userId/posts/contributions/pending/$friendId").get().addOnSuccessListener { snapshot ->
                snapshot.children.forEach {
                    val prodId = it.key!!
                    var index = it.childrenCount
                    index--
                    friendRef.child("interests/$prodId").get().addOnSuccessListener {
                            posts -> posts.getValue(Post::class.java)?.let { it -> options.add(it) }
                        if(index <= 0){
                            adapter.setPostsList(options)
                        }
                    }

                }
            }
        }

    }
}