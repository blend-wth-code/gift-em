package com.example.giftem.screens

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import com.bumptech.glide.Glide
import com.example.giftem.R
import com.example.giftem.models.Post
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class UploadPostActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private var imageUri: Uri? = null
    private var imageView: ImageView? = null
    private lateinit var userId: String
    private var imageData: ByteArray? = null

    private lateinit var prodName: String
    private var price: Double = 0.0
    private lateinit var desc: String
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_post)
        userId = intent.getStringExtra("userId")!!
        findViewById<TextView>(R.id.profileMenu).setOnClickListener { handleProfileMenu() }
        findViewById<TextView>(R.id.homeMenu).setOnClickListener { handleHomeMenu() }

        if(userId.isNullOrBlank()){
            Toast.makeText(this, "Invalid User", Toast.LENGTH_SHORT).show();
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }
        findViewById<Button>(R.id.upload).setOnClickListener{handleUpload()}
        findViewById<Button>(R.id.pick).setOnClickListener{handlePick()}
        findViewById<Button>(R.id.takePicture).setOnClickListener{ handleTakePicture() }
        findViewById<TextView>(R.id.logout).setOnClickListener { handleLogout() }

        cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitMap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                imageView?.setImageBitmap(imageBitMap)
                val boas = ByteArrayOutputStream()
                imageBitMap.compress(Bitmap.CompressFormat.PNG, 100, boas)
                imageData = boas.toByteArray()
                Glide.with(this)
                    .load(imageUri.toString())
                    .into(findViewById(R.id.prodImg))
            }
        }
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

    private fun handleTakePicture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, (findViewById<EditText>(R.id.prodName)).text.toString())
            values.put(MediaStore.Images.Media.DESCRIPTION, findViewById<EditText>(R.id.desc).text.toString())
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            // Create an Intent to capture an image
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

            // Start the camera activity and wait for a result
            cameraResultLauncher.launch(intent)
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun handlePick() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), 1)
    }

    private fun handleUpload() {
        val fileName = UUID.randomUUID().toString()
        val name = findViewById<EditText>(R.id.prodName)
        var isValid = true
        if (name.text.isNullOrBlank()){
            name.error ="Product Name is required"
            isValid = false
        }
        else prodName = name.text.toString()

        desc = if (findViewById<EditText>(R.id.desc).text.isNullOrBlank()){
            ""
        } else{
            findViewById<EditText>(R.id.desc).text.trim().toString()
        }

        val enteredPrice = findViewById<EditText>(R.id.price)
        if(enteredPrice.text.isNullOrBlank()){
            enteredPrice.error = "Price should be between 1 and 9999999999"
            isValid = false
        }
        else if (!enteredPrice.text.toString().isDigitsOnly()){
            isValid = false
            enteredPrice.error = "Price should be a number"
        }
        else{
            price = enteredPrice.text.toString().toDouble()
        }
        if(price > 0 && price <= 9999999999){
            price = String.format("%.2f", enteredPrice.text.toString().toDouble()).toDouble()
        }
        else{
            enteredPrice.error = "Price should be between 1 and 9999999999"
        }


        if(!isValid) return

        val ref = FirebaseStorage.getInstance().getReference("users/${userId}/posts/${fileName}")
        if(imageData == null){
            Toast.makeText(this, "Product Image is required.", Toast.LENGTH_SHORT).show()
            return
        }
        val uploadTask = ref.putBytes(imageData!!)
        uploadTask.addOnSuccessListener {uri ->
            uri.storage.downloadUrl
                .addOnCompleteListener {img ->
                    val postRef = FirebaseDatabase.getInstance().getReference("users/$userId/posts/interests")
                    val newPost = Post(fileName, prodName, price, 0.0, img.result.toString(), desc, userId)
                    postRef.child(fileName).setValue(newPost)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("userId", userId)
                    startActivity(intent)
                    Toast.makeText(this, "Image upload successful", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload successful but download URL retrieval failed.", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Toast.makeText(this, "Image upload failed.", Toast.LENGTH_SHORT).show()
        }
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
                    .into(findViewById(R.id.prodImg))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}