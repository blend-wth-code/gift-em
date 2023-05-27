package com.example.giftem.screens

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.example.giftem.R
import com.example.giftem.models.Post
import com.google.firebase.database.FirebaseDatabase

class ContributeActivity : AppCompatActivity() {
    private lateinit var userId:String
    private lateinit var postId:String
    private lateinit var friendId:String
    private val database = FirebaseDatabase.getInstance()
    private lateinit var post:Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contribute)
        userId = intent.getStringExtra("userId")!!
        postId = intent.getStringExtra("postId")!!
        friendId = intent.getStringExtra("friendId")!!

        findViewById<TextView>(R.id.profileMenu).setOnClickListener { handleProfileMenu() }
        findViewById<TextView>(R.id.homeMenu).setOnClickListener { handleHomeMenu() }
        findViewById<TextView>(R.id.addPostMenu).setOnClickListener{handleAddPostMenu()}
        findViewById<TextView>(R.id.logout).setOnClickListener { handleLogout() }


        database.reference.child("users/$friendId/posts/interests/$postId").get().addOnSuccessListener {
            post = it.getValue(Post::class.java)!!
            val prodImg = findViewById<ImageView>(R.id.productImage)
            Glide.with(applicationContext).load(post.prodImg).into(prodImg)
            findViewById<TextView>(R.id.productTitle).text = post.prodName
            findViewById<TextView>(R.id.productDescription).text = post.desc
            findViewById<TextView>(R.id.productPrice).text = "${post.price} CAD"
        }

        val cart = findViewById<Button>(R.id.addToCart)
        database.reference.child("users/$userId/posts/contributions/pending/$friendId/$postId").get().addOnSuccessListener {
            if(it.value != null){
                cart.visibility = View.GONE
            }
            else{
                cart.visibility = View.VISIBLE
            }
        }
        cart.setOnClickListener{
            database.reference.child("users/$userId/posts/contributions/pending/$friendId/$postId").setValue(post.price).addOnSuccessListener {
                database.reference.child("users/$friendId/posts/myContributions/pending/$postId").setValue(post.price)
                cart.visibility = View.GONE
                Toast.makeText(this, "Product Added to Cart", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.checkout).setOnClickListener{
            database.reference.child("users/$userId/posts/contributions/pending/$friendId/$postId").setValue(post.price).addOnSuccessListener {
                database.reference.child("users/$friendId/posts/myContributions/pending/$postId").setValue(post.price).addOnSuccessListener {
                    val intent = Intent(this, CheckoutActivity::class.java)
                    intent.putExtra("postId", postId)
                    intent.putExtra("friendId", friendId)
                    intent.putExtra("userId", userId)
                    startActivity(intent)
                }
            }
        }

        findViewById<Button>(R.id.viewCart).setOnClickListener{
            database.reference.child("users/$userId/posts/contributions/pending/$friendId").get().addOnSuccessListener {
                if(it.childrenCount > 0){
                    val intent = Intent(this, CheckoutActivity::class.java)
                    intent.putExtra("friendId", friendId)
                    intent.putExtra("userId", userId)
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this, "No items in cart", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun handleProfileMenu() {
        val intent = Intent(this, MyProfileActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
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

}