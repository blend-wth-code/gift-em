package com.example.giftem.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.giftem.R
import com.example.giftem.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        findViewById<Button>(R.id.login).setOnClickListener{handleLogin()}
        findViewById<TextView>(R.id.registerLink)
            .setOnClickListener{startActivity(Intent(this, RegisterActivity::class.java))}
    }

    private fun handleLogin(){
        val email = findViewById<TextView>(R.id.email).text.toString()
        val pwd = findViewById<TextView>(R.id.pwd).text.toString()
        var isValid = true

        if(email.isNullOrBlank()){
            findViewById<TextView>(R.id.email).error = "Email is required"
            isValid = false
        }
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            findViewById<TextView>(R.id.email).error = "Invalid Email Address"
            isValid = false
        }
        if(pwd.isNullOrBlank()){
            findViewById<TextView>(R.id.pwd).error = "Password is required"
            isValid = false
        }
        else if(pwd.length < 6){
            findViewById<TextView>(R.id.pwd).error = "Password should have minimum 5 chars"
            isValid = false
        }

        if(!isValid){
            //display error
            return
        }
        else{
            val auth = FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("userId", auth.uid.toString())
                        startActivity(intent)
                    } else {
                        //display error
                    }
                }.addOnFailureListener{
                    Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
