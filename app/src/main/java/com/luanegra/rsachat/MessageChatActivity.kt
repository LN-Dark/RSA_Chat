package com.luanegra.rsachat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.luanegra.rsachat.adapterClasses.ChatAdapter
import com.luanegra.rsachat.modelclasses.Chat
import com.luanegra.rsachat.modelclasses.Users
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessageChatActivity : AppCompatActivity() {

    var userIdVisit: String = ""
    var firebaseUser: FirebaseUser? = null
    var chatsAdapter: ChatAdapter? = null
    var mChatList: List<Chat>? = null
    lateinit var recycler_messagechat: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        intent = intent
        userIdVisit = intent.getStringExtra("reciever_id").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val write_messagechat: EditText = findViewById(R.id.write_messagechat)
        val send_messagechat: ImageView = findViewById(R.id.send_messagechat)
        val reciever_profileImage: de.hdodenhof.circleimageview.CircleImageView = findViewById(R.id.profileimage_messagechat)
        val reciever_UserName: TextView = findViewById(R.id.username_messagechat)
        val btn_atach_image: ImageView = findViewById(R.id.atach_image_messagechat)
        recycler_messagechat = findViewById(R.id.recycler_messagechat)
        recycler_messagechat.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recycler_messagechat.layoutManager = linearLayoutManager
        btn_atach_image.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Select Image"), 438)
        }

        val userRecieverRef = FirebaseDatabase.getInstance().reference.child("users").child(userIdVisit)
        userRecieverRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
            val user: Users? = snapshot.getValue(Users::class.java)
                Glide.with(this@MessageChatActivity).load(user!!.getprofile()).placeholder(R.drawable.profile_1).into(reciever_profileImage)
                reciever_UserName.text = user.getusername()
                send_messagechat.setOnClickListener{
                    if(!write_messagechat.text.toString().equals("")){
                        sendMessage(firebaseUser!!.uid, userIdVisit, write_messagechat.text.toString())
                        write_messagechat.setText("")
                    }else{
                        Toast.makeText(this@MessageChatActivity, "Can't send empty messages.", Toast.LENGTH_LONG).show()
                    }
                }
                retrieveChatMessages(firebaseUser!!.uid, userIdVisit, user.getprofile())
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun retrieveChatMessages(senderId: String, receiverID: String, receiverImageUrl: String?) {
        mChatList = ArrayList()
        val chatsReference = FirebaseDatabase.getInstance().reference.child("Chats")
        val chatsListsReference = FirebaseDatabase.getInstance().reference.child("ChatsLists")
        chatsReference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for(valor in snapshot.children){
                    val chat = valor.getValue(Chat::class.java)
                    if(chat!!.getreciever().equals(receiverID) && chat!!.getsender().equals(senderId) || chat!!.getreciever().equals(senderId) && chat!!.getsender().equals(receiverID)){
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatsAdapter = ChatAdapter(this@MessageChatActivity, (mChatList as ArrayList<Chat>), receiverImageUrl.toString())
                    recycler_messagechat.adapter = chatsAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }

    private fun sendMessage(senderId: String, recieverId: String, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        val formatted = current.format(formatter)
        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["reciever"] = recieverId
        messageHashMap["message"] = message
        messageHashMap["timeStamp"] =formatted
        messageHashMap["isseen"] = false
        messageHashMap["messageId"] = messageKey
        messageHashMap["url"] = ""
        reference.child("Chats").child(messageKey!!).setValue(messageHashMap).addOnCompleteListener { task ->
            if(task.isSuccessful){
                val chatListsRef = FirebaseDatabase.getInstance().reference.child("ChatLists").child(firebaseUser!!.uid).child(userIdVisit)
                chatListsRef.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                       if(!snapshot.exists()){
                           chatListsRef.child("id").setValue(userIdVisit)
                           val chatListsRecieverRef = FirebaseDatabase.getInstance().reference.child("ChatLists").child(userIdVisit).child(firebaseUser!!.uid)
                           chatListsRecieverRef.child("id").setValue(firebaseUser!!.uid)
                       }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })







                val usersRef = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 438 && resultCode == RESULT_OK && data != null && data!!.data != null){
            val loadingBar = ProgressDialog(this)
            loadingBar.setMessage("Please wait...")
            loadingBar.show()
            val fileUri = data.data
            val storageRef = FirebaseStorage.getInstance().reference.child("Chat_Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageRef.child("$messageId.jpg")
            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)
            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if(task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val downloadUrl = task.result
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                    val formatted = current.format(formatter)
                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["reciever"] = userIdVisit
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["timeStamp"] = formatted
                    messageHashMap["isseen"] = "false"
                    messageHashMap["messageId"] = messageId
                    messageHashMap["url"] = downloadUrl.toString()
                    ref.child("Chats").child(messageId!!).setValue(messageHashMap).addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            val chatListsRef = FirebaseDatabase.getInstance().reference.child("ChatLists").child("id").setValue(firebaseUser!!.uid)
                            val usersRef = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid)
                        }
                    }
                    loadingBar.dismiss()
                }
            }

        }
    }

}