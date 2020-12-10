package com.luanegra.rsachat.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.luanegra.rsachat.AutenticationActivity
import com.luanegra.rsachat.MessageChatActivity
import com.luanegra.rsachat.RSA.DecryptGenerator
import com.luanegra.rsachat.modelclasses.Chat
import com.luanegra.rsachat.modelclasses.Users

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(mRemoteMessage: RemoteMessage) {
        super.onMessageReceived(mRemoteMessage)
        val sented = mRemoteMessage.data["sented"]
        val user = mRemoteMessage.data["user"]
        val sharedpref = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentOnlineUser = sharedpref.getString("currentUser", "none")
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if(firebaseUser != null && sented == firebaseUser.uid){
            if(currentOnlineUser != user){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    sendOREONotification(mRemoteMessage)
                }else{
                    sendNotification(mRemoteMessage)
                }
            }
        }
    }

    private fun sendNotification(mRemoteMessage: RemoteMessage) {
        val user = mRemoteMessage.data["user"]
        val icon = mRemoteMessage.data["icon"]
        val title = mRemoteMessage.data["title"]
        val body = mRemoteMessage.data["body"]
        val context = this
        val notification = mRemoteMessage.notification
        val j = user!!.replace("[\\D]".toRegex(), "").toInt()
        FirebaseDatabase.getInstance().reference.child("users").child(user).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                val intent = Intent(context, AutenticationActivity::class.java)
                intent.putExtra("reciever_id", user!!.getUid())
                intent.putExtra("reciever_profile", user.getprofile())
                intent.putExtra("reciever_username", user.getusername())
                intent.putExtra("activityType", "notification")
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pedingIntent = PendingIntent.getActivity(context, j, intent, PendingIntent.FLAG_ONE_SHOT)
                val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val builder: NotificationCompat.Builder = NotificationCompat.Builder(context)
                    .setSmallIcon(icon!!.toInt())
                    .setContentTitle(title)
                    .setContentText(decryptMessage(body.toString()))
                    .setAutoCancel(true)
                    .setSound(defaultSound)
                    .setContentIntent(pedingIntent)
                val noti = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                var i = 0
                if(j > 0){
                    i = j
                }
                noti.notify(i, builder.build())
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }

    fun decryptMessage(message: String): String {

            val sharedPreference =  getSharedPreferences("RSA_CHAT", Context.MODE_PRIVATE)
            val plainText = DecryptGenerator.generateDecrypt(encryptText = message, privateKey = sharedPreference.getString("privateKey",""))

        return plainText.toString()
    }

    private fun sendOREONotification(mRemoteMessage: RemoteMessage) {
        val user = mRemoteMessage.data["user"]
        val icon = mRemoteMessage.data["icon"]
        val title = mRemoteMessage.data["title"]
        val body = decryptMessage(mRemoteMessage.data["body"].toString())
        val notification = mRemoteMessage.notification
        val j = user!!.replace("[\\D]".toRegex(), "").toInt()
        val context = this
        FirebaseDatabase.getInstance().reference.child("users").child(user).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                val intent = Intent(context, AutenticationActivity::class.java)
                intent.putExtra("reciever_id", user!!.getUid())
                intent.putExtra("reciever_profile", user.getprofile())
                intent.putExtra("reciever_username", user.getusername())
                intent.putExtra("activityType", "notification")
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pedingIntent = PendingIntent.getActivity(context, j, intent, PendingIntent.FLAG_ONE_SHOT)
                val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val oreoNotification = OreoNotification(context)
                val builder: Notification.Builder = oreoNotification.getOreoNotification(title, body, pedingIntent, defaultSound, icon)
                var i = 0

                if(j > 0){
                    i = j
                }

                oreoNotification.getManager!!.notify(i, builder.build())
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }
}