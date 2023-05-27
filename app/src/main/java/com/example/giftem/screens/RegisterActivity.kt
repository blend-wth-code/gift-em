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
import com.example.giftem.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        findViewById<TextView>(R.id.loginLink)
            .setOnClickListener{ startActivity(Intent(this, LoginActivity::class.java))}
        findViewById<Button>(R.id.register).setOnClickListener{handleRegister()}
    }

    private fun handleRegister(){
        val fname = findViewById<TextView>(R.id.fname)
        val lname = findViewById<TextView>(R.id.lname)
        val email = findViewById<TextView>(R.id.email)
        val pwd = findViewById<TextView>(R.id.pwd)
        val confirmPwd = findViewById<TextView>(R.id.confirmPwd)
        var isValid = true

        if(fname.text.isNullOrBlank()){
            fname.error = "First Name is required"
            isValid = false
        }
        else if(fname.text.length <= 3){
            fname.error = "First Name should have minimum 4 chars"
            isValid = false
        }
        if(lname.text.isNullOrBlank()){
            lname.error = "Last Name is required"
            isValid = false
        }
        else if(lname.text.length <= 3){
            lname.error = "First Name should have minimum 4 chars"
            isValid = false
        }
        if(email.text.isNullOrBlank()){
            email.error = "Email is required"
            isValid = false
        }
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email.text).matches()){
            email.error = "Invalid Email Address"
            isValid = false
        }
        if(pwd.text.isNullOrBlank()){
            pwd.error = "Password is required"
            isValid = false
        }
        else if(pwd.text.length < 6){
            pwd.error = "Password should have minimum 5 chars"
            isValid = false
        }
        if(confirmPwd.text.isNullOrBlank()){
            confirmPwd.error = "Confirm Password is required"
            isValid = false
        }
        else if(confirmPwd.text.toString() != pwd.text.toString()){
            confirmPwd.error = "Confirm Password not matching with Password"
            isValid = false
        }

        if(!isValid){
            Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show()
        }
        else{
            val auth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(email.text.toString(), pwd.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val ref = FirebaseDatabase.getInstance().reference
                        val user = User(auth.uid.toString(),
                            fname.text.toString(), lname.text.toString())
                        ref.child("users/${auth.uid.toString()}").setValue(user)
                        val intent = Intent(this, LoginActivity::class.java)
                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                    } else {
                        TODO("display error")
                    }
                }

        }
    }
}
