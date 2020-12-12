package com.luanegra.rsachat.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.luanegra.rsachat.AutenticationActivity
import com.luanegra.rsachat.R
import com.luanegra.rsachat.adapterClasses.UserAdapter
import com.luanegra.rsachat.modelclasses.ChatList
import com.luanegra.rsachat.modelclasses.Users
import com.luanegra.rsachat.notifications.Token


class ChatFragment : Fragment() {
    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>?= null
    private var usersChatList: List<ChatList>? = null
    lateinit var recycler_chats: RecyclerView
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_chat, container, false)
        recycler_chats = view.findViewById(R.id.recycler_chats)
        recycler_chats.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.stackFromEnd = false
        recycler_chats.layoutManager = linearLayoutManager
        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersChatList = ArrayList()
        val chatListsRef = FirebaseDatabase.getInstance().reference.child("ChatLists").child(firebaseUser!!.uid)
        chatListsRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (usersChatList as ArrayList).clear()
                for(datasnap in snapshot.children){
                    val chat = datasnap.getValue(ChatList::class.java)
                    (usersChatList as ArrayList).add(chat!!)
                }
                retrieveChatList()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        updateToken(FirebaseInstanceId.getInstance().token)

        return view
    }

    private fun updateToken(token: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = Token(token.toString())
        ref.child(firebaseUser!!.uid).setValue(token1)
    }

    private val usersRef = FirebaseDatabase.getInstance().reference.child("users")
    private var retrieveEventListener: ValueEventListener? = null
    private fun retrieveChatList(){
        mUsers = ArrayList()
        retrieveEventListener = usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList).clear()
                for(datasnap in snapshot.children){
                    val user = datasnap.getValue(Users::class.java)
                    for(eachChatList in usersChatList!!){
                        if(user!!.getUid() == eachChatList.getid()){
                            (mUsers as ArrayList).add(user)
                        }
                    }
                }
                userAdapter = UserAdapter(context!!, (mUsers as ArrayList<Users>), true, 1)
                recycler_chats.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    override fun onPause() {
        super.onPause()
        usersRef.removeEventListener(retrieveEventListener!!)
    }

}