package com.luanegra.rsachat

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.luanegra.rsachat.RSA.GenerateKeys

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@RegisterActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        val btn_register = findViewById<Button>(R.id.btn_register)
        btn_register.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username: EditText = findViewById<EditText>(R.id.username_register)
        val email: EditText = findViewById<EditText>(R.id.email_register)
        val password: EditText = findViewById<EditText>(R.id.password_register)

        if(username.text.toString() == ""){
            Toast.makeText(this@RegisterActivity, "Write username.", Toast.LENGTH_LONG).show()
        }else if(email.text.toString() == ""){
            Toast.makeText(this@RegisterActivity, "Write email.", Toast.LENGTH_LONG).show()
        }else if(password.text.toString() == ""){
            Toast.makeText(this@RegisterActivity, "Write password.", Toast.LENGTH_LONG).show()
        }else{
            mAuth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString()).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    firebaseUserId = mAuth.currentUser!!.uid
                    refUsers = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUserId)
                    val userHashMap = HashMap<String, Any>()
                    userHashMap["uid"] = firebaseUserId
                    userHashMap["username"] = username.text.toString()
                    userHashMap["email"] = email.text.toString()
                    userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/rsachat-73eff.appspot.com/o/profile_1.png?alt=media&token=1eba6856-99ec-4d5f-9eed-47561c59f11c"
                    userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/rsachat-73eff.appspot.com/o/coverdefault.jpg?alt=media&token=30312c5f-a8a2-4ed6-91ed-470d89c3a7bc"
                    userHashMap["status"] = "offline"
                    userHashMap["search"] = username.text.toString().toLowerCase()
                    userHashMap["facebook"] = "https://m.facebook.com"
                    userHashMap["instagram"] = "https://m.instagram.com"
                    userHashMap["website"] = "https://www.google.pt"
                    val gerarChaves = GenerateKeys()
                    var listKeys: List<String>?
                    listKeys = ArrayList()
                    listKeys = gerarChaves.generateKeys()
                    userHashMap["publicKey"] = listKeys.get(0)

                    val sharedPreference =  getSharedPreferences("RSA_CHAT",Context.MODE_PRIVATE)
                    val editor = sharedPreference.edit()
                    editor.putString("privateKey",listKeys.get(1))
                    editor.putString("publicKey",listKeys.get(0))
                    editor.apply()

                    refUsers.updateChildren(userHashMap).addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }else{
                            Toast.makeText(this@RegisterActivity, "Error Message:  " + task.exception!!.message, Toast.LENGTH_LONG).show()
                        }
                    }

                }else{
                    Toast.makeText(this@RegisterActivity, "Error Message:  " + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }

    }


}