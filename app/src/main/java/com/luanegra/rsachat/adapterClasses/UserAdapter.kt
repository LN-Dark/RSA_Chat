package com.luanegra.rsachat.adapterClasses

import android.content.Context
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luanegra.rsachat.R
import com.luanegra.rsachat.modelclasses.Users
import com.squareup.picasso.Picasso

class UserAdapter(mContext: Context, mUserList: List<Users>, isChatCheck: Boolean) : RecyclerView.Adapter<UserAdapter.ViewHolder?>() {
    private val mContext: Context
    private val mUserList: List<Users>
    private var isChatCheck: Boolean

    init {
        this.mContext = mContext
        this.mUserList = mUserList
        this.isChatCheck = isChatCheck
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txt_userName: TextView
        var txt_lastMessage: TextView
        var image_profile: de.hdodenhof.circleimageview.CircleImageView
        var image_online: de.hdodenhof.circleimageview.CircleImageView
        var image_offline: de.hdodenhof.circleimageview.CircleImageView

        init {
            txt_userName = itemView.findViewById(R.id.user_name_search)
            txt_lastMessage = itemView.findViewById(R.id.message_last_search)
            image_profile = itemView.findViewById(R.id.profile_image_search)
            image_online = itemView.findViewById(R.id.image_online_search)
            image_offline = itemView.findViewById(R.id.image_offline_search)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.usersearch_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: Users? = mUserList[position]
        holder.txt_userName.text = user!!.getusername()
        Picasso.get().load(user!!.getprofile()).placeholder(R.drawable.ic_person_24px).into(holder.image_profile)
        if(user.getstatus() == ""){
           // holder.image_online.visibility =
        }else{
           // holder.image_offline.visibility =
        }
    }

    override fun getItemCount(): Int {
        return mUserList.size
    }
}