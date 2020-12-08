package com.luanegra.rsachat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.luanegra.rsachat.RSA.DecryptGenerator
import com.luanegra.rsachat.RSA.EncryptGenerator
import com.luanegra.rsachat.RSA.GenerateKeys
import com.luanegra.rsachat.adapterClasses.ChatAdapter
import com.luanegra.rsachat.fragments.APIService
import com.luanegra.rsachat.modelclasses.Chat
import com.luanegra.rsachat.modelclasses.Users
import com.luanegra.rsachat.notifications.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.PublicKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessageChatActivity : AppCompatActivity() {

    var userIdVisit: String = ""
    var firebaseUser: FirebaseUser? = null
    var chatsAdapter: ChatAdapter? = null
    var mChatList: List<Chat>? = null
    lateinit var recycler_messagechat: RecyclerView
    var userRecieverRef: DatabaseReference? = null
    var notify = false
    var apiService: APIService? = null
    var publicKeyVisit: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        val toolbar: Toolbar = findViewById(R.id.toolbar_message_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

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
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recycler_messagechat.layoutManager = linearLayoutManager
        btn_atach_image.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Select Image"), 438)
        }

        userRecieverRef = FirebaseDatabase.getInstance().reference.child("users").child(userIdVisit)
        userRecieverRef!!.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
            val user: Users? = snapshot.getValue(Users::class.java)
                Glide.with(this@MessageChatActivity).load(user!!.getprofile()).placeholder(R.drawable.profile_1).into(reciever_profileImage)
                reciever_UserName.text = user.getusername()
                send_messagechat.setOnClickListener{
                    if(!write_messagechat.text.toString().equals("")){
                        notify = true
                        publicKeyVisit = user.getpublicKey().toString()
                        sendMessage(firebaseUser!!.uid, userIdVisit, write_messagechat.text.toString(), publicKeyVisit)
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
        seenMessage(userIdVisit)
    }

    private fun retrieveChatMessages(senderId: String, receiverID: String, receiverImageUrl: String?) {
        mChatList = ArrayList()
        val chatsReference = FirebaseDatabase.getInstance().reference.child("Chats")
        val chatsListsReference = FirebaseDatabase.getInstance().reference.child("ChatsLists")
        chatsReference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for(valor in snapshot.children){
                    var chat = valor.getValue(Chat::class.java)
                    if(chat!!.getreciever().equals(receiverID) && chat!!.getsender().equals(senderId) || chat!!.getreciever().equals(senderId) && chat!!.getsender().equals(receiverID)){
                            (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatsAdapter = ChatAdapter(applicationContext, (mChatList as ArrayList<Chat>), receiverImageUrl.toString())
                    recycler_messagechat.adapter = chatsAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }

    private fun sendMessage(senderId: String, recieverId: String, message: String, publicKey: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        val formatted = current.format(formatter)
        var messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["reciever"] = recieverId
        messageHashMap["timeStamp"] =formatted
        messageHashMap["isseen"] = false
        messageHashMap["messageId"] = messageKey
        messageHashMap["url"] = ""
        val textEnc: String = encryptMessage(message, publicKey)
        messageHashMap["message"] = textEnc
        val sharedPreference =  getSharedPreferences("RSA_CHAT", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString(messageKey, message)
        editor.apply()
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
            }
        }
        val usersRef = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid)
        usersRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                if(notify){
                    sendNotification(recieverId, user!!.getusername(), textEnc)
                }
                notify = false

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun sendNotification(recieverId: String, getusername: String?, message: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val query = ref.orderByKey().equalTo(recieverId)

        query.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(dataSnapshot in snapshot.children){
                    val token: Token? = dataSnapshot.getValue(Token::class.java)
                    val data  = Data(firebaseUser!!.uid, R.mipmap.ic_launcher, "$message", "New message from $getusername", userIdVisit)
                    val sender = Sender(data!!, token!!.getToken().toString())
                    apiService!!.sendNotification(sender).enqueue(object: Callback<MyResponse>{
                        override fun onResponse(
                            call: Call<MyResponse>,
                            response: Response<MyResponse>
                        ) {
                            if(response.code() == 200){
                                if(response.body()!!.success != 1){
                                    Toast.makeText(this@MessageChatActivity, "Failed, Nothing happen.", Toast.LENGTH_LONG).show()
                                }
                            }

                        }

                        override fun onFailure(call: Call<MyResponse>, t: Throwable) {

                        }

                    })


                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 438 && resultCode == RESULT_OK && data != null && data!!.data != null){
            notify = true
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
                    var messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["reciever"] = userIdVisit
                    messageHashMap["timeStamp"] = formatted
                    messageHashMap["isseen"] = false
                    messageHashMap["messageId"] = messageId
                    messageHashMap["url"] = downloadUrl.toString()
                    val textEnc: String = encryptMessage("sent you an image.", publicKeyVisit)
                    messageHashMap["message"] = textEnc
                    val sharedPreference =  getSharedPreferences("RSA_CHAT", Context.MODE_PRIVATE)
                    val editor = sharedPreference.edit()
                    editor.putString(messageId, "sent you an image.")
                    editor.apply()
                    ref.child("Chats").child(messageId!!).setValue(messageHashMap).addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            val usersRef = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid)
                            usersRef.addValueEventListener(object: ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val user = snapshot.getValue(Users::class.java)
                                    if(notify){
                                        sendNotification(userIdVisit, user!!.getusername(), "sent you an image.")
                                    }
                                    notify = false
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                        }
                    }
                    loadingBar.dismiss()
                }
            }

        }
    }

    var seenListener: ValueEventListener? = null
    private fun seenMessage (userID: String){
        val chatsRef = FirebaseDatabase.getInstance().reference.child("Chats")
        seenListener = chatsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(datasnap in snapshot.children){
                    val chat = datasnap.getValue(Chat::class.java)
                    if(chat!!.getreciever().equals(firebaseUser!!.uid) && chat.getsender().equals(userID)){
                        val messageHashMap = HashMap<String, Any?>()
                        messageHashMap["isseen"] = true
                        datasnap.ref.updateChildren(messageHashMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onPause() {
        super.onPause()
        userRecieverRef!!.removeEventListener(seenListener!!)
        updateStatus("offline")
    }

    fun encryptMessage(message: String, publicKey: String): String{
        val encryptedString = EncryptGenerator.generateEncrypt(plainText = message, publicKey = publicKey, jWEAlgorithm = "RSA-OAEP-256", encryptionMethod = "A256CBC-HS512")
        return encryptedString.toString()
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

}