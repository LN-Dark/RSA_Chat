package com.luanegra.rsachat

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.luanegra.rsachat.RSA.EncryptGenerator
import com.luanegra.rsachat.adapterClasses.ChatAdapter
import com.luanegra.rsachat.fragments.APIService
import com.luanegra.rsachat.modelclasses.Blocked
import com.luanegra.rsachat.modelclasses.Chat
import com.luanegra.rsachat.modelclasses.Users
import com.luanegra.rsachat.notifications.*
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.launch
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessageChatActivity : AppCompatActivity() {

    var userIdVisit: String = ""
    var firebaseUser: FirebaseUser? = null
    var chatsAdapter: ChatAdapter? = null
    var mChatList: List<Chat>? = null
    lateinit var recycler_messagechat: RecyclerView
    private var userRecieverRef: DatabaseReference? = null
    var notify = false
    var apiService: APIService? = null
    var publicKeyVisit: String = ""
    var recieverName: String = ""
    var recieverProfileImage: String = ""
    var showNotificationUser: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        val toolbar: Toolbar = findViewById(R.id.toolbar_message_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("resultAUTH", "true")
            startActivity(intent)
            finish()
        }
        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

        intent = intent
        userIdVisit = intent.getStringExtra("reciever_id").toString()
        publicKeyVisit = intent.getStringExtra("publicKeyVisit").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val write_messagechat: TextInputEditText = findViewById(R.id.write_messagechat)
        val send_messagechat: ImageView = findViewById(R.id.send_messagechat)
        val reciever_profileImage: de.hdodenhof.circleimageview.CircleImageView = findViewById(R.id.profileimage_messagechat)
        val reciever_UserName: TextView = findViewById(R.id.username_messagechat)
        val btn_atach_image: ImageView = findViewById(R.id.atach_image_messagechat)
        recycler_messagechat = findViewById(R.id.recycler_messagechat)
        recycler_messagechat.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        recycler_messagechat.layoutManager = linearLayoutManager
        val refblockedUsers = FirebaseDatabase.getInstance().reference
        refblockedUsers.child("BlockedUsers").child(userIdVisit).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for(dataSnapshot in snapshot.children){
                        val userID: Blocked? = dataSnapshot.getValue(Blocked::class.java)
                        if(userID!!.getuserID() == firebaseUser!!.uid){
                            write_messagechat.setText(getString(R.string.yourareblocked))
                            write_messagechat.isEnabled = false
                            send_messagechat.isEnabled = false
                            btn_atach_image.isEnabled = false
                        }
                    }
                }else{
                    write_messagechat.isEnabled = true
                    send_messagechat.isEnabled = true
                    btn_atach_image.isEnabled = true
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        btn_atach_image.setOnClickListener {
            intent.putExtra("resultAUTH", "true")
            easyImage = EasyImage.Builder(this)
                .setCopyImagesToPublicGalleryFolder(false)
                .setFolderName("EasyImage sample")
                .allowMultiple(false)
                .build()
            easyImage!!.openChooser(this)
        }

        userRecieverRef = FirebaseDatabase.getInstance().reference.child("users").child(userIdVisit)
        userRecieverRef!!.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                reciever_profileImage.load(user!!.getprofile())
                recieverProfileImage = user.getprofile()
                reciever_UserName.text = user.getusername()
                recieverName = user.getusername()
                showNotificationUser = user.getnotificationsShow()
                send_messagechat.setOnClickListener{
                    if(write_messagechat.text.toString() != ""){
                        notify = true
                        publicKeyVisit = user.getpublicKey()
                        sendMessage(firebaseUser!!.uid, userIdVisit, write_messagechat.text.toString(), publicKeyVisit)
                        write_messagechat.setText("")
                    }else{
                        Toast.makeText(this@MessageChatActivity, getString(R.string.cantsendemptymessages), Toast.LENGTH_LONG).show()
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
        chatsReference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for(valor in snapshot.children){
                    val chat = valor.getValue(Chat::class.java)
                    if(chat!!.getreciever().equals(receiverID) && chat.getsender().equals(senderId) || chat.getreciever().equals(senderId) && chat.getsender().equals(receiverID)){
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

    private fun sendMessage(senderId: String, recieverId: String, message: String, publicKey: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        val formatted = current.format(formatter)
        val messageHashMap = HashMap<String, Any?>()
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
        if(showNotificationUser){
            val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
            val query = ref.orderByKey().equalTo(recieverId)

            query.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(dataSnapshot in snapshot.children){
                        val token: Token? = dataSnapshot.getValue(Token::class.java)
                        val data  = Data(firebaseUser!!.uid, R.mipmap.ic_launcher,
                            message, "New message from $getusername", userIdVisit)
                        val sender = Sender(data, token!!.getToken().toString())
                        apiService!!.sendNotification(sender).enqueue(object: Callback<MyResponse>{
                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {
                                if(response.code() == 200){
                                    if(response.body()!!.success != 1){
                                        Toast.makeText(this@MessageChatActivity, getString(R.string.failednothinghappened), Toast.LENGTH_LONG).show()
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
    }

    var easyImage: EasyImage? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && data != null && data.data != null){
            notify = true
            val loadingBar = ProgressDialog(this)
            val storageRef = FirebaseStorage.getInstance().reference.child("Chat_Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageRef.child("$messageId.jpg")
            val fileRef = filePath.child(
                System.currentTimeMillis().toString() + ".jpg"
            )
            easyImage!!.handleActivityResult(
                requestCode,
                resultCode,
                data,
                this,
                object : DefaultCallback() {
                    override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                        lifecycleScope.launch {
                            val compressedImageFile = Compressor.compress(this@MessageChatActivity, imageFiles[0].file){
                                quality(50)
                                format(Bitmap.CompressFormat.JPEG)
                            }
                            if(compressedImageFile.length() < 30000000){
                                loadingBar.setMessage(getString(R.string.pleasewait))
                                loadingBar.setCancelable(false)
                                loadingBar.show()
                                val uploadTask2: UploadTask = fileRef.putBytes(compressedImageFile.readBytes())
                                uploadTask2.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                                    if (task.isSuccessful) {
                                        task.exception?.let {
                                            throw it
                                        }
                                    }
                                    return@Continuation fileRef.downloadUrl
                                }).addOnCompleteListener { task ->
                                    if(task.isSuccessful){
                                        val downloadUrl = task.result
                                        val current = LocalDateTime.now()
                                        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                                        val formatted = current.format(formatter)
                                        val messageHashMap = HashMap<String, Any?>()
                                        messageHashMap["sender"] = firebaseUser!!.uid
                                        messageHashMap["reciever"] = userIdVisit
                                        messageHashMap["timeStamp"] = formatted
                                        messageHashMap["isseen"] = false
                                        messageHashMap["messageId"] = messageId
                                        messageHashMap["url"] = downloadUrl.toString()
                                        val textEnc: String = encryptMessage(getString(R.string.sentyouanimage), publicKeyVisit)
                                        messageHashMap["message"] = textEnc
                                        val sharedPreference =  getSharedPreferences("RSA_CHAT", Context.MODE_PRIVATE)
                                        val editor = sharedPreference.edit()
                                        editor.putString(messageId, "sent you an image.")
                                        editor.apply()
                                        ref.child("Chats").child(messageId!!).setValue(messageHashMap).addOnCompleteListener { task1 ->
                                            if(task1.isSuccessful){
                                                val usersRef = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid)
                                                usersRef.addValueEventListener(object: ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        val user = snapshot.getValue(Users::class.java)
                                                        if(notify){
                                                            sendNotification(userIdVisit, user!!.getusername(), getString(R.string.sentyouanimage))
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
                            }else{
                                Toast.makeText(this@MessageChatActivity, getString(R.string.imagetoobig), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                })
        }
    }

    private var seenListener: ValueEventListener? = null
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

    private fun encryptMessage(message: String, publicKey: String): String{
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
        if(!intent.getStringExtra("resultAUTH").equals("true")){
            val intent = Intent(this, AutenticationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("reciever_id", userIdVisit)
            intent.putExtra("reciever_profile", recieverProfileImage)
            intent.putExtra("reciever_username", recieverName)
            intent.putExtra("activityType", "notification")
            startActivity(intent)
            finish()
        }else{
            intent.putExtra("resultAUTH", "false")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("resultAUTH", "true")
        startActivity(intent)
        finish()
    }
}