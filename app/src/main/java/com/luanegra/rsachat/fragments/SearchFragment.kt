package com.luanegra.rsachat.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.luanegra.rsachat.R
import com.luanegra.rsachat.adapterClasses.UserAdapter
import com.luanegra.rsachat.modelclasses.Users


class SearchFragment : Fragment() {
    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>?= null
    private var recycler_search: RecyclerView? = null
    private var txt_search: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_search, container, false)
        mUsers = ArrayList()
        recycler_search = view.findViewById(R.id.recycler_search)
        recycler_search!!.setHasFixedSize(true)
        recycler_search!!.layoutManager = LinearLayoutManager(context)
        txt_search = view.findViewById(R.id.txt_search)
        retrieveAllUsers()
        txt_search!!.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                retrieveSearchUsers(s.toString().toLowerCase())
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        return view
    }

    private fun retrieveAllUsers() {
        var firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        var refUsers: DatabaseReference? = null
        refUsers = FirebaseDatabase.getInstance().reference.child("users")
        refUsers.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                recycler_search?.adapter?.notifyDataSetChanged()
                if(txt_search!!.text.toString() == ""){
                    if(snapshot.exists()){
                        for(user in snapshot.children){
                            val newuser: Users? = user.getValue(Users::class.java)
                            if(!(newuser!!.getUid()).equals(firebaseUser)){
                                (mUsers as ArrayList<Users>).add(newuser)
                            }
                        }
                        userAdapter = UserAdapter(context!!, mUsers!!, false)
                        recycler_search!!.adapter = userAdapter
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun retrieveSearchUsers(searchname: String) {
        var firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val queryUsers = FirebaseDatabase.getInstance().reference.child("users").orderByChild("search").startAt(searchname).endAt(searchname + "\uf8ff")
        queryUsers.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                recycler_search?.adapter?.notifyDataSetChanged()
                if(snapshot.exists()){
                    for(user in snapshot.children){
                        val newuser: Users? = user.getValue(Users::class.java)
                        if(!(newuser!!.getUid()).equals(firebaseUser)){
                            (mUsers as ArrayList<Users>).add(newuser)
                        }
                    }
                    userAdapter = UserAdapter(context!!, mUsers!!, false)
                    recycler_search!!.adapter = userAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}