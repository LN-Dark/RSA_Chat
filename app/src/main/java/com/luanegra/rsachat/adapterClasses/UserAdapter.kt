package com.luanegra.rsachat.adapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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
import com.luanegra.rsachat.modelclasses.Chat
import com.luanegra.rsachat.modelclasses.Users
import de.hdodenhof.circleimageview.CircleImageView


class UserAdapter(mContext: Context, mUserList: List<Users>, isChatCheck: Boolean) : RecyclerView.Adapter<UserAdapter.ViewHolder?>() {
    private val mContext = mContext
    private val mUserList: List<Users>
    private var isChatCheck: Boolean
    private var lastMsg: String = ""

    init {
        this.mUserList = mUserList
        this.isChatCheck = isChatCheck
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txt_userName: TextView
        var txt_lastMessage: TextView
        var image_profile: CircleImageView
        var image_online: CircleImageView
        var image_offline: CircleImageView

        init {
            txt_userName = itemView.findViewById(R.id.user_name_search)
            txt_lastMessage = itemView.findViewById(R.id.message_last_search)
            image_profile = itemView.findViewById(R.id.profile_image_search)
            image_online = itemView.findViewById(R.id.image_online_search)
            image_offline = itemView.findViewById(R.id.image_offline_search)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(
            R.layout.usersearch_item_layout,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: Users? = mUserList[position]
        holder.txt_userName.text = user!!.getusername()
        holder.image_profile.load(user.getprofile())

        holder.itemView.setOnClickListener {

            val mDialogView = LayoutInflater.from(mContext).inflate(
                R.layout.alertdialog_profile_options,
                null
            )

            val mBuilder = AlertDialog.Builder(mContext)
                .setView(mDialogView)
            val  mAlertDialog = mBuilder.show()

            val dialogimageview: CircleImageView = mDialogView.findViewById(R.id.profile_dialog)
            dialogimageview.load(user!!.getprofile())

            mDialogView.findViewById<Button>(R.id.perfil_dialog_show).setOnClickListener {
                val intent = Intent(mContext, VisitProfileActivity::class.java)
                intent.putExtra("reciever_id", user.getUid())
                mContext.startActivity(intent)
                mAlertDialog.dismiss()
            }

            mDialogView.findViewById<Button>(R.id.chat_dialog_show).setOnClickListener {
                val intent = Intent(mContext, MessageChatActivity::class.java)
                intent.putExtra("reciever_id", user.getUid())
                intent.putExtra("reciever_profile", user.getprofile())
                intent.putExtra("reciever_username", user.getusername())
                mContext.startActivity(intent)
                mAlertDialog.dismiss()
            }

        }

        if(isChatCheck){
           retrieveLasMessage(user.getUid(), holder.txt_lastMessage, mUserList[position].getusername())
        }else{
          holder.txt_lastMessage.visibility = View.GONE
        }
        if(user.getstatus().equals(mContext.getString(R.string.online))){
            holder.image_online.visibility = View.VISIBLE
            holder.image_offline.visibility = View.GONE
        }else{
            holder.image_online.visibility = View.GONE
            holder.image_offline.visibility = View.VISIBLE
        }
    }

    private fun retrieveLasMessage(uid: String?, txtLastmessage: TextView, recieverUserName: String?) {
        lastMsg = "defaultMsg"
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference
        reference.child("Chats").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(dataSnapshot in snapshot.children){
                    var chat: Chat? = dataSnapshot.getValue(Chat::class.java)
                    if(firebaseUser != null && chat != null){
                        if(chat.getreciever() == firebaseUser!!.uid && chat.getsender() == uid || chat.getreciever() == uid && chat.getsender() == firebaseUser!!.uid){
                            if(chat.getsender().equals(firebaseUser!!.uid)){
                                chat = decryptMessage(chat, 0)
                                chat.setmessage(mContext.getString(R.string.me) + chat.getmessage())
                            }else{
                                chat = decryptMessage(chat, 1)
                                chat.setmessage(recieverUserName + ": " + chat.getmessage())
                            }
                            lastMsg = chat.getmessage()!!
                        }
                    }
                }
                when(lastMsg){
                    mContext.getString(R.string.no_message) -> txtLastmessage.text = mContext.getString(R.string.no_message)
                    mContext.getString(R.string.sentyouanimage) -> txtLastmessage.text = mContext.getString(R.string.image_sent)
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
            val plainText = chat.getmessage()?.let { DecryptGenerator.generateDecrypt(encryptText = it, privateKey = sharedPreference.getString("privateKey","")) }
            chat.setmessage(plainText!!.toString())
        }
        return chat
    }

    override fun getItemCount(): Int {
        return mUserList.size
    }
}