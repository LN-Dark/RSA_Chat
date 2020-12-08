package com.luanegra.rsachat

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.luanegra.rsachat.RSA.GenerateKeys

class WelcomeActivity : AppCompatActivity() {

    var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        val btn_register = findViewById<Button>(R.id.btn_register_welcome)
        btn_register.setOnClickListener{
            val intent = Intent(this@WelcomeActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btn_login = findViewById<Button>(R.id.btn_login_welcome)
        btn_login.setOnClickListener{
            val intent = Intent(this@WelcomeActivity, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //Firebase Auth instance
        mAuth = FirebaseAuth.getInstance()
        val sign_in_btn = findViewById<SignInButton>(R.id.google_sigin)
        sign_in_btn.setOnClickListener {
            signIn()
        }
        
    }

    override fun onStart() {
        super.onStart()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        if(firebaseUser != null){
            val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        private const val RC_SIGN_IN = 120
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserId: String = ""

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    Toast.makeText(this@WelcomeActivity, "Error Message:  " + e.message, Toast.LENGTH_LONG).show()
                }
            } else {
                if(task.exception.toString().contains("125000") || task.exception.toString().contains("10")){
                    Toast.makeText(this@WelcomeActivity, "Your Google Play Store don't let you sign in, please use the other method of Registration/LogIn", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this@WelcomeActivity, "Error Message:  " + task.exception.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        firebaseUserId = mAuth.currentUser!!.uid
                        refUsers = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUserId)
                        val userHashMap = HashMap<String, Any>()
                        userHashMap["uid"] = firebaseUserId
                        userHashMap["username"] = account.displayName.toString()
                        userHashMap["email"] = account.email.toString()
                        userHashMap["profile"] = account.photoUrl.toString()
                        userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/rsachat-73eff.appspot.com/o/coverdefault.jpg?alt=media&token=30312c5f-a8a2-4ed6-91ed-470d89c3a7bc"
                        userHashMap["status"] = "offline"
                        userHashMap["search"] = account.displayName.toString().toLowerCase()
                        userHashMap["facebook"] = "https://m.facebook.com"
                        userHashMap["instagram"] = "https://m.instagram.com"
                        userHashMap["website"] = "https://www.google.pt"
                        val gerarChaves = GenerateKeys()
                        var listKeys: List<String>?
                        listKeys = ArrayList()
                        listKeys = gerarChaves.generateKeys()
                        userHashMap["publicKey"] = listKeys.get(0)

                        val sharedPreference =  getSharedPreferences("RSA_CHAT", Context.MODE_PRIVATE)
                        val editor = sharedPreference.edit()
                        editor.putString("privateKey",listKeys.get(1))
                        editor.putString("publicKey",listKeys.get(0))
                        editor.apply()
                        refUsers.updateChildren(userHashMap).addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }else{
                                Toast.makeText(this@WelcomeActivity, "Error Message:  " + task.exception!!.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(this@WelcomeActivity, "Error Message:  " + task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                }
    }



}