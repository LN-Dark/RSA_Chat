package com.luanegra.rsachat.adapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.luanegra.rsachat.MessageChatActivity
import com.luanegra.rsachat.R
import com.luanegra.rsachat.RSA.DecryptGenerator
import com.luanegra.rsachat.VisitProfileActivity
import com.luanegra.rsachat.modelclasses.Blocked
import com.luanegra.rsachat.modelclasses.Chat
import com.luanegra.rsachat.modelclasses.Users
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream


class UserAdapter(mContext: Context, mUserList: List<Users>, isChatCheck: Boolean, type: Int) : RecyclerView.Adapter<UserAdapter.ViewHolder?>() {
    private val mContext = mContext
    private val mUserList: List<Users> = mUserList
    private var isChatCheck: Boolean
    private var lastMsg: String = ""
    var visualType: Int = type

    init {
        this.isChatCheck = isChatCheck
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txt_userName: TextView = itemView.findViewById(R.id.user_name_search)
        var txt_lastMessage: TextView
        var image_profile: CircleImageView
        var image_online: CircleImageView
        var image_offline: CircleImageView

        init {
            txt_lastMessage = itemView.findViewById(R.id.message_last_search)
            image_profile = itemView.findViewById(R.id.profile_image_search)
            image_online = itemView.findViewById(R.id.image_online_search)
            image_offline = itemView.findViewById(R.id.image_offline_search)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View?
        if(visualType == 0){
            view = LayoutInflater.from(mContext).inflate(
                R.layout.usersearch_item_layout,
                parent,
                false
            )
        }else{
            view = LayoutInflater.from(mContext).inflate(
                R.layout.userchat_item_layout,
                parent,
                false
            )
        }

        return ViewHolder(view!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: Users = mUserList[position]
        holder.txt_userName.text = user.getusername()
//        val shre = PreferenceManager.getDefaultSharedPreferences(mContext)
//        val previouslyEncodedImage = shre.getString(user.getUid(), "")
//        if(!previouslyEncodedImage.equals("")){
//            retrieveIMG(user.getUid())
//        }else{
            holder.image_profile.load(user.getprofile())
//            saveIMG(holder.image_profile.drawToBitmap())
//        }


        holder.itemView.setOnClickListener {

            val mDialogView = LayoutInflater.from(mContext).inflate(
                R.layout.alertdialog_profile_options,
                null
            )

            val mBuilder = AlertDialog.Builder(mContext)
                .setView(mDialogView)
            val  mAlertDialog = mBuilder.show()

            val dialogimageview: CircleImageView = mDialogView.findViewById(R.id.profile_dialog)
            dialogimageview.load(user.getprofile())

            mDialogView.findViewById<Button>(R.id.perfil_dialog_show).setOnClickListener {
                val intent = Intent(mContext, VisitProfileActivity::class.java)
                intent.putExtra("reciever_id", user.getUid())

                intent.putExtra("resultAUTH", "true")
                mContext.startActivity(intent)
                mAlertDialog.dismiss()
            }
            var controllerBlock = 0
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val refblockedUsers = FirebaseDatabase.getInstance().reference.child("BlockedUsers").child(
                firebaseUser!!.uid
            )
            refblockedUsers.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {
                            val userID: Blocked? = dataSnapshot.getValue(Blocked::class.java)
                            if (userID!!.getuserID() == user.getUid()) {
                                controllerBlock = 1
                            }
                        }
                        if (controllerBlock == 0) {
                            mDialogView.findViewById<Button>(R.id.chat_block).text =
                                mContext.getString(
                                    R.string.block
                                )
                        } else {
                            mDialogView.findViewById<Button>(R.id.chat_block).text =
                                mContext.getString(
                                    R.string.unblock
                                )
                        }
                    } else {
                        mDialogView.findViewById<Button>(R.id.chat_block).text = mContext.getString(
                            R.string.block
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
            mDialogView.findViewById<Button>(R.id.chat_block).setOnClickListener {
                blockUser(user.getUid(), user.getusername())
                mAlertDialog.dismiss()
            }
            mDialogView.findViewById<Button>(R.id.chat_dialog_show).setOnClickListener {
                val intent = Intent(mContext, MessageChatActivity::class.java)
                intent.putExtra("reciever_id", user.getUid())
                intent.putExtra("reciever_profile", user.getprofile())
                intent.putExtra("reciever_username", user.getusername())
                intent.putExtra("publicKeyVisit", user.getpublicKey())
                intent.putExtra("resultAUTH", "true")
                mContext.startActivity(intent)
                mAlertDialog.dismiss()
            }
        }

        if(isChatCheck){
           retrieveLasMessage(
               user.getUid(),
               holder.txt_lastMessage,
               mUserList[position].getusername()
           )
        }else{
          holder.txt_lastMessage.visibility = View.GONE
        }
        if(user.getstatus() == mContext.getString(R.string.online)){
            holder.image_online.visibility = View.VISIBLE
            holder.image_offline.visibility = View.GONE
        }else{
            holder.image_online.visibility = View.GONE
            holder.image_offline.visibility = View.VISIBLE
        }
    }

    fun saveIMG(bmp: Bitmap){
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        val encodedImage: String = Base64.encodeToString(b, Base64.DEFAULT)
        val shre = PreferenceManager.getDefaultSharedPreferences(mContext)
        val edit: SharedPreferences.Editor = shre.edit()
        edit.putString("image_data", encodedImage)
        edit.apply()
    }

    fun retrieveIMG(userID: String?): Bitmap{
        var bmp: Bitmap? = null
        val shre = PreferenceManager.getDefaultSharedPreferences(mContext)
        val previouslyEncodedImage = shre.getString(userID, "")
        if (!previouslyEncodedImage.equals("", ignoreCase = true)) {
            val b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
            bmp = bitmap
        }
        return bmp!!
    }

    private fun blockUser(usertoblock: String, username: String){
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refblockedUsers = FirebaseDatabase.getInstance().reference.child("BlockedUsers").child(
            firebaseUser!!.uid
        )
        var controllerBlock = 0
        refblockedUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val userID: Blocked? = dataSnapshot.getValue(Blocked::class.java)
                        if (userID!!.getuserID() == usertoblock) {
                            FirebaseDatabase.getInstance().reference.child("BlockedUsers").child(
                                firebaseUser.uid
                            ).child(userID.getuid()).removeValue()
                            controllerBlock = 1
                            Toast.makeText(
                                mContext,
                                mContext.getString(R.string.unblockeduser) + username,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    if (controllerBlock == 0) {
                        val userHashMap = HashMap<String, Any>()
                        val idBlock = refblockedUsers.push().key.toString()
                        userHashMap["uid"] = idBlock
                        userHashMap["userID"] = usertoblock
                        userHashMap["conditionBlock"] = "true"
                        FirebaseDatabase.getInstance().reference.child("BlockedUsers").child(
                            firebaseUser.uid
                        ).child(idBlock).updateChildren(userHashMap)
                        Toast.makeText(
                            mContext,
                            mContext.getString(R.string.blockeduser) + username,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val userHashMap = HashMap<String, Any>()
                    val idBlock = refblockedUsers.push().key.toString()
                    userHashMap["uid"] = idBlock
                    userHashMap["userID"] = usertoblock
                    userHashMap["conditionBlock"] = "true"
                    FirebaseDatabase.getInstance().reference.child("BlockedUsers").child(
                        firebaseUser.uid
                    ).child(idBlock).updateChildren(userHashMap)
                    Toast.makeText(
                        mContext,
                        mContext.getString(R.string.blockeduser) + username,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun retrieveLasMessage(
        uid: String?,
        txtLastmessage: TextView,
        recieverUserName: String?
    ) {
        lastMsg = "defaultMsg"
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference
        reference.child("Chats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    var chat: Chat? = dataSnapshot.getValue(Chat::class.java)
                    if (firebaseUser != null && chat != null) {
                        if (chat.getreciever() == firebaseUser.uid && chat.getsender() == uid || chat.getreciever() == uid && chat.getsender() == firebaseUser.uid) {
                            if (chat.getsender().equals(firebaseUser.uid)) {
                                chat = decryptMessage(chat, 0)
                                chat.setmessage(mContext.getString(R.string.me) + chat.getmessage())
                            } else {
                                chat = decryptMessage(chat, 1)
                                chat.setmessage(recieverUserName + ": " + chat.getmessage())
                            }
                            lastMsg = chat.getmessage()!!
                        }
                    }
                }
                when (lastMsg) {
                    mContext.getString(R.string.no_message) -> txtLastmessage.text =
                        mContext.getString(
                            R.string.no_message
                        )
                    mContext.getString(R.string.sentyouanimage) -> txtLastmessage.text =
                        mContext.getString(
                            R.string.image_sent
                        )
                    else -> txtLastmessage.text = lastMsg
                }
                lastMsg = mContext.getString(R.string.defaultmsg)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun decryptMessage(chat: Chat, who: Int): Chat {
        if(who == 0){
            val sharedPreference =  mContext.getSharedPreferences("RSA_CHAT", Context.MODE_PRIVATE)
            chat.setmessage(sharedPreference.getString(chat.getMessageid(), "").toString())
        }else{
            val sharedPreference =  mContext.getSharedPreferences("RSA_CHAT", Context.MODE_PRIVATE)
            val plainText = chat.getmessage()?.let { DecryptGenerator.generateDecrypt(
                encryptText = it, privateKey = sharedPreference.getString(
                    "privateKey",
                    ""
                )
            ) }
            chat.setmessage(plainText!!.toString())
        }
        return chat
    }

    override fun getItemCount(): Int {
        return mUserList.size
    }

}