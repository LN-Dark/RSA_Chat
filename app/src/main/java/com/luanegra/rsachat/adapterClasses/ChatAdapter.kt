package com.luanegra.rsachat.adapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.luanegra.rsachat.MessageChatActivity
import com.luanegra.rsachat.R
import com.luanegra.rsachat.modelclasses.Chat
import com.luanegra.rsachat.modelclasses.Users

class ChatAdapter(mContext: Context, mChatList: List<Chat>, image_url: String) : RecyclerView.Adapter<ChatAdapter.ViewHolder?>() {
    private val mContext: Context
    private val mChatList: List<Chat>
    private var image_url: String
    val firebaseUser = FirebaseAuth.getInstance().currentUser!!
    val userRecieverRef = FirebaseDatabase.getInstance().reference.child("users")

    init {
        this.mContext = mContext
        this.mChatList = mChatList
        this.image_url = image_url
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var profile_image_message: de.hdodenhof.circleimageview.CircleImageView
        var message_chat: TextView
        var image_view: ImageView

        var text_seen: TextView

        init {
            profile_image_message = itemView.findViewById(R.id.profile_image_message)
            message_chat = itemView.findViewById(R.id.message_chat)
            image_view = itemView.findViewById(R.id.image_view)
            text_seen = itemView.findViewById(R.id.text_seen)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if(viewType == 1){
            val view: View = LayoutInflater.from(mContext).inflate(
                R.layout.message_item_right,
                parent,
                false
            )
            ViewHolder(view)
        }else{
            val view: View = LayoutInflater.from(mContext).inflate(
                R.layout.message_item_left,
                parent,
                false
            )
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat: Chat = mChatList[position]
        if(chat.getsender().equals(firebaseUser!!.uid)){
            if(chat.getmessage().equals("sent you an image.") && !chat.geturl().equals("")){
                holder.image_view.visibility = View.VISIBLE
                holder.message_chat.visibility = View.GONE
                Glide.with(mContext).load(chat.geturl()).into(holder.image_view)
            }else{
                holder.image_view.visibility = View.GONE
                holder.message_chat.visibility = View.VISIBLE
                holder.message_chat.text = chat.getmessage()
            }
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid)
            userRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    Glide.with(mContext).load(user!!.getprofile()).into(holder.profile_image_message)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        }else{
            if(chat.getmessage().equals("sent you an image.") && !chat.geturl().equals("")){
                holder.image_view.visibility = View.VISIBLE
                holder.message_chat.visibility = View.GONE
                Glide.with(mContext).load(chat.geturl()).into(holder.image_view)
            }else{
                holder.image_view.visibility = View.GONE
                holder.message_chat.visibility = View.VISIBLE
                holder.message_chat.text = chat.getmessage()
            }
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(chat.getsender().toString())
            userRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    Glide.with(mContext).load(user!!.getprofile()).into(holder.profile_image_message)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }

        if(position == mChatList.size-1){
            if(chat.getisseen()!!){
                holder.text_seen.text = "${chat.gettimeStamp()} - Seen"
                if(chat.getmessage().equals("sent you an image.") && !chat.geturl().equals("")){
                    val lp: RelativeLayout.LayoutParams? = holder.text_seen.layoutParams as RelativeLayout.LayoutParams?
                    lp!!.setMargins(0, 125, 10, 0)
                    holder.text_seen.layoutParams = lp
                }
            }else{
                holder.text_seen.text = "${chat.gettimeStamp()} - Sent"
                if(chat.getmessage().equals("sent you an image.") && !chat.geturl().equals("")){
                    val lp: RelativeLayout.LayoutParams? = holder.text_seen.layoutParams as RelativeLayout.LayoutParams?
                    lp!!.setMargins(0, 125, 10, 0)
                    holder.text_seen.layoutParams = lp
                }
            }
        }else{
            holder.text_seen.text = "${chat.gettimeStamp()}"
        }

    }

    override fun getItemCount(): Int {
        return mChatList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(firebaseUser.uid.equals(mChatList[position].getsender())){
            1
        }else{
            0
        }
    }
}