package com.luanegra.rsachat

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.luanegra.rsachat.modelclasses.Users

class VisitProfileActivity : AppCompatActivity() {

    var refUsers: DatabaseReference? = null
    var firebaseUser: FirebaseUser?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_profile)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_visitprofile)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        val cover_visitprofile: ImageView = findViewById(R.id.cover_visitprofile)
        val profileimage_visitprofile: de.hdodenhof.circleimageview.CircleImageView = findViewById(R.id.profileimage_visitprofile)
        val facebook_visitprofile: ImageView = findViewById(R.id.facebook_visitprofile)
        val instagram_visitprofile: ImageView = findViewById(R.id.instagram_visitprofile)
        val website_visitprofile: ImageView = findViewById(R.id.website_visitprofile)
        val btn_sendmessage: Button = findViewById(R.id.button_sendmessageUser)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance().reference.child("users").child(intent.getStringExtra("reciever_id").toString())
        refUsers!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                Glide.with(this@VisitProfileActivity).load(user!!.getprofile()).placeholder(R.drawable.profile_1).into(profileimage_visitprofile)
                Glide.with(this@VisitProfileActivity).load(user!!.getcover()).placeholder(R.drawable.coverdefault).into(cover_visitprofile)
                supportActionBar!!.title = user!!.getusername()
                facebook_visitprofile.setOnClickListener {
                    val uri = Uri.parse(user!!.getfacebook())
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)

                }
                instagram_visitprofile.setOnClickListener {
                    val uri = Uri.parse(user!!.getinstagram())
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
                website_visitprofile.setOnClickListener {
                    val uri = Uri.parse(user!!.getwebsite())
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
                btn_sendmessage.setOnClickListener{
                    val intent = Intent(this@VisitProfileActivity, MessageChatActivity::class.java)
                    intent.putExtra("reciever_id", user!!.getUid())
                    intent.putExtra("reciever_profile", user!!.getprofile())
                    intent.putExtra("reciever_username", user!!.getusername())
                    startActivity(intent)
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
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
}