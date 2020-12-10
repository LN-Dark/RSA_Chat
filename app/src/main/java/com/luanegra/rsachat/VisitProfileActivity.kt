package com.luanegra.rsachat

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import coil.load
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.luanegra.rsachat.modelclasses.Users
import de.hdodenhof.circleimageview.CircleImageView

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
        val profileimage_visitprofile: CircleImageView = findViewById(R.id.profileimage_visitprofile)
        val facebook_visitprofile: ImageView = findViewById(R.id.facebook_visitprofile)
        val instagram_visitprofile: ImageView = findViewById(R.id.instagram_visitprofile)
        val website_visitprofile: ImageView = findViewById(R.id.website_visitprofile)
        val btn_sendmessage: Button = findViewById(R.id.button_sendmessageUser)
        val aboutme_visitprofile: TextInputEditText = findViewById(R.id.aboutme_visitprofile)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance().reference.child("users").child(intent.getStringExtra("reciever_id").toString())
        refUsers!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                profileimage_visitprofile.load(user!!.getprofile())
                profileimage_visitprofile.setOnClickListener{
                    val mDialogView = LayoutInflater.from(this@VisitProfileActivity).inflate(
                        R.layout.profileimageview,
                        null
                    )

                    val mBuilder = AlertDialog.Builder(this@VisitProfileActivity)
                        .setView(mDialogView)
                    val  mAlertDialog = mBuilder.show()

                    val dialogimageview: CircleImageView = mDialogView.findViewById(R.id.img_profileimageview)
                    dialogimageview.load(user!!.getprofile())

                    mDialogView.findViewById<Button>(R.id.btn_closeprofileimageview).setOnClickListener {
                        mAlertDialog.dismiss()
                    }
                }
                aboutme_visitprofile.setText(user.getaboutMe())
                cover_visitprofile.load(user!!.getcover())
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