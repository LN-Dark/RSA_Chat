package com.luanegra.rsachat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import coil.load
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.luanegra.rsachat.fragments.ChatFragment
import com.luanegra.rsachat.fragments.SearchFragment
import com.luanegra.rsachat.fragments.SettingsFragment
import com.luanegra.rsachat.modelclasses.Users
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

class MainActivity : AppCompatActivity() {
    private var refUsers: DatabaseReference? = null
    var firebaseUser: FirebaseUser?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        val tableLayout: TabLayout = findViewById(R.id.tab_layout)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        val user_name: TextView = findViewById(R.id.user_name)
        val profile_image: CircleImageView = findViewById(R.id.profile_image)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(ChatFragment(), getString(R.string.chats))
        viewPagerAdapter.addFragment(SearchFragment(), getString(R.string.search))
        viewPagerAdapter.addFragment(SettingsFragment(), getString(R.string.myprofile))
        viewPager.adapter = viewPagerAdapter
        tableLayout.setupWithViewPager(viewPager)
        refUsers!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user: Users? = snapshot.getValue(Users::class.java)
                    user_name.text = user!!.getusername()
                    profile_image.load(user.getprofile())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, getString(R.string.errormessage) + error.message, Toast.LENGTH_LONG).show()
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                val map = HashMap<String, Any>()
                map["status"] = "offline"
                refUsers!!.updateChildren(map).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                }

            return true
            }
        }
        return false
    }
    

    internal  class  ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager){
        private val fragments: ArrayList<Fragment> = ArrayList<Fragment>()
        private val titles: ArrayList<String> = ArrayList<String>()

        override fun getCount(): Int {
            return fragments.size

        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        fun addFragment(fragment: Fragment, title: String){
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }
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
            startActivity(intent)
            finish()
        }else{
            intent.putExtra("resultAUTH", "false")
        }
    }

    override fun onPause() {
        super.onPause()
        updateStatus("offline")
    }
}