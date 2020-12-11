package com.luanegra.rsachat.adapterClasses

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.luanegra.rsachat.R
import com.luanegra.rsachat.RSA.DecryptGenerator
import com.luanegra.rsachat.ViewFullImageActivity
import com.luanegra.rsachat.modelclasses.Chat
import com.luanegra.rsachat.modelclasses.Users

class ChatAdapter(mContext: Context?, mChatList: List<Chat>, imageurl: String) : RecyclerView.Adapter<ChatAdapter.ViewHolder?>() {
    private val mContext: Context? = mContext!!
    private val mChatList: List<Chat>
    private var imageurl: String
    val firebaseUser = FirebaseAuth.getInstance().currentUser!!

    init {
        this.mChatList = mChatList
        this.imageurl = imageurl
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var profileimagemessage: de.hdodenhof.circleimageview.CircleImageView = itemView.findViewById(R.id.profile_image_message)
        var message_chat: TextView
        var image_view: ImageView
        var text_seen: TextView

        init {
            message_chat = itemView.findViewById(R.id.message_chat)
            image_view = itemView.findViewById(R.id.image_view)
            text_seen = itemView.findViewById(R.id.text_seen)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if(viewType == 1){
            val view: View = LayoutInflater.from(mContext!!).inflate(
                R.layout.message_item_right,
                parent,
                false
            )
            ViewHolder(view)
        }else{
            val view: View = LayoutInflater.from(mContext!!).inflate(
                R.layout.message_item_left,
                parent,
                false
            )
            ViewHolder(view)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var chat: Chat = mChatList[position]
        if(chat.getsender().equals(firebaseUser.uid)){
            chat = decryptMessage(chat, 0)
            if(chat.getmessage().equals("sent you an image.") && !chat.geturl().equals("")){
                holder.image_view.visibility = View.VISIBLE
                holder.message_chat.visibility = View.GONE
                holder.image_view.load(chat.geturl())
                holder.image_view.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        mContext!!.getString(R.string.viewfullimage),
                        mContext.getString(R.string.deletemessage),
                        mContext.getString(R.string.cancel)
                    )
                    val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
                    builder.setTitle(mContext.getString(R.string.chooseanoption))
                    builder.setItems(options) { dialog, which ->
                        if (which == 0) {
                            val intent = Intent(mContext, ViewFullImageActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            val userRef =
                                FirebaseDatabase.getInstance().reference.child("users").child(
                                    chat.getreciever()
                                )
                            userRef.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val user: Users? = snapshot.getValue(Users::class.java)
                                    intent.putExtra("url", chat.geturl())
                                    intent.putExtra("reciever_id", chat.getreciever())
                                    intent.putExtra("reciever_profile", user!!.getprofile())
                                    intent.putExtra("reciever_username", user.getusername())
                                    intent.putExtra("publicKeyVisit", user.getpublicKey())
                                    mContext.startActivity(intent)
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                        } else if (which == 1) {
                            deleteSentMessage(position)
                        } else if (which == 2) {
                            dialog.dismiss()
                        }
                    }
                    builder.show()
                }
            }else{
                holder.image_view.visibility = View.GONE
                holder.message_chat.visibility = View.VISIBLE
                holder.message_chat.text = chat.getmessage()
                holder.message_chat.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        mContext!!.getString(R.string.deletemessage),
                        mContext.getString(R.string.cancel)
                    )
                    val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Choose an option:")
                    builder.setItems(options) { dialog, which ->
                        if (which == 0) {
                            deleteSentMessage(position)
                        } else if (which == 1) {
                            dialog.dismiss()
                        }
                    }
                    builder.show()
                }
            }
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser.uid)
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user: Users? = snapshot.getValue(Users::class.java)
                        holder.profileimagemessage.load(user!!.getprofile())
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }else{
            chat = decryptMessage(chat, 1)
            if(chat.getmessage().equals("sent you an image.") && !chat.geturl().equals("")){
                holder.image_view.visibility = View.VISIBLE
                holder.message_chat.visibility = View.GONE
                holder.image_view.load(chat.geturl())
                holder.image_view.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        mContext!!.getString(R.string.viewfullimage),
                        mContext.getString(R.string.cancel)
                    )
                    val builder: AlertDialog.Builder = AlertDialog.Builder(
                        holder.itemView.context
                    )
                    builder.setTitle(mContext.getString(R.string.chooseanoption))
                    builder.setItems(options) { dialog, which ->
                        if (which == 0) {
                            val intent = Intent(mContext, ViewFullImageActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.putExtra("url", chat.geturl())
                            mContext.startActivity(intent)
                        } else if (which == 1) {
                            dialog.dismiss()
                        }
                    }
                    builder.show()
                }
            }else{
                holder.image_view.visibility = View.GONE
                holder.message_chat.visibility = View.VISIBLE
                holder.message_chat.text = chat.getmessage()
            }
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(
                chat.getsender().toString()
            )
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    holder.profileimagemessage.load(user!!.getprofile())
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }

        if(position == mChatList.size-1){
            if(getItemViewType(position) == 1){
                if(chat.getisseen()!!){
                    holder.text_seen.text = "${chat.gettimeStamp()}" + mContext!!.getString(R.string.seen_message)
                    if(chat.getmessage().equals("sent you an image.") && !chat.geturl().equals("")){
                        val lp: RelativeLayout.LayoutParams? = holder.text_seen.layoutParams as RelativeLayout.LayoutParams?
                        lp!!.setMargins(0, 125, 10, 0)
                        holder.text_seen.layoutParams = lp
                    }
                }else{
                    holder.text_seen.text = "${chat.gettimeStamp()}" + mContext!!.getString(R.string.sent_message)
                    if(chat.getmessage().equals("sent you an image.") && !chat.geturl().equals("")){
                        val lp: RelativeLayout.LayoutParams? = holder.text_seen.layoutParams as RelativeLayout.LayoutParams?
                        lp!!.setMargins(0, 125, 10, 0)
                        holder.text_seen.layoutParams = lp
                    }
                }
            }else{
                holder.text_seen.text = "${chat.gettimeStamp()}"
            }
        }else{
            holder.text_seen.text = "${chat.gettimeStamp()}"
        }

    }

    override fun getItemCount(): Int {
        return mChatList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(firebaseUser.uid == mChatList[position].getsender()){
            1
        }else{
            0
        }
    }

    private fun decryptMessage(chat: Chat, who: Int): Chat{
        if(who == 0){
            val sharedPreference =  mContext!!.getSharedPreferences(
                "RSA_CHAT",
                Context.MODE_PRIVATE
            )
            chat.setmessage(sharedPreference.getString(chat.getMessageid(), "").toString())
        }else{
            val sharedPreference =  mContext!!.getSharedPreferences(
                "RSA_CHAT",
                Context.MODE_PRIVATE
            )
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

    private fun deleteSentMessage(position: Int){
        FirebaseDatabase.getInstance().reference.child("Chats").child(
            mChatList[position].getMessageid()!!
        ).removeValue()

    }
}