package com.luanegra.rsachat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class ViewFullImageActivity : AppCompatActivity() {
    private var image_view: ImageView? = null
    private var image_url: String = ""
    private var reciever_id: String = ""
    private var reciever_profile: String = ""
    private var reciever_username: String = ""
    var firebaseUser: FirebaseUser?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_image)
        image_view = findViewById(R.id.viewfullimage)
        image_url = intent.getStringExtra("url").toString()
        reciever_id = intent.getStringExtra("reciever_id").toString()
        reciever_profile = intent.getStringExtra("reciever_profile").toString()
        reciever_username = intent.getStringExtra("reciever_username").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_viewfullimage)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.imagefrom) + " $reciever_username"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MessageChatActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("reciever_id", reciever_id)
            intent.putExtra("reciever_profile", reciever_profile)
            intent.putExtra("reciever_username", reciever_username)
            startActivity(intent)
            finish()
        }
        image_view!!.load(image_url)
    }

    private fun updateStatus(status: String){
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid)
        val userHashMap = HashMap<String, Any>()
        userHashMap["status"] = status
        ref.updateChildren(userHashMap)
    }

    override fun onResume() {
        super.onResume()
        updateStatus("online")
    }

    override fun onPause() {
        super.onPause()
        updateStatus("offline")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MessageChatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("reciever_id", reciever_id)
        intent.putExtra("reciever_profile", reciever_profile)
        intent.putExtra("reciever_username", reciever_username)
        startActivity(intent)
        finish()
    }
}