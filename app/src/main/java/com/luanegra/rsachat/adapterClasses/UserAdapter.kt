package com.luanegra.rsachat.adapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.luanegra.rsachat.MessageChatActivity
import com.luanegra.rsachat.R
import com.luanegra.rsachat.modelclasses.Users



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
        Glide.with(mContext).load(user!!.getprofile()).placeholder(R.drawable.profile_1).into(holder.image_profile)

        holder.itemView.setOnClickListener {

            val mDialogView = LayoutInflater.from(mContext).inflate(
                R.layout.alertdialog_profile_options,
                null
            )

            val mBuilder = AlertDialog.Builder(mContext)
                .setView(mDialogView)
            val  mAlertDialog = mBuilder.show()
            Glide.with(mContext).load(user!!.getprofile()).placeholder(R.drawable.profile_1).into(
                mDialogView.findViewById(R.id.profile_dialog)
            )

            mDialogView.findViewById<Button>(R.id.perfil_dialog_show).setOnClickListener {
                mAlertDialog.dismiss()
            }

            mDialogView.findViewById<Button>(R.id.chat_dialog_show).setOnClickListener {
                val intent = Intent(mContext, MessageChatActivity::class.java)
                intent.putExtra("reciever_id", user!!.getUid())
                intent.putExtra("reciever_profile", user!!.getprofile())
                intent.putExtra("reciever_username", user!!.getusername())
                mContext.startActivity(intent)
                mAlertDialog.dismiss()
            }

        }

        if(user.getstatus() == "off"){
           // holder.image_online.visibility =

        }else{
           // holder.image_offline.visibility =

        }
    }

    override fun getItemCount(): Int {
        return mUserList.size
    }
}