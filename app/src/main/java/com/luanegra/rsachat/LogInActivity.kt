package com.luanegra.rsachat

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LogInActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_login)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "LogIn"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@LogInActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        val btn_logIn = findViewById<Button>(R.id.btn_login)
        btn_logIn.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email: TextInputEditText = findViewById(R.id.email_login)
        val password: TextInputEditText = findViewById(R.id.password_login)

        when {
            email.text.toString() == "" -> {
                Toast.makeText(this@LogInActivity, getString(R.string.writeemail), Toast.LENGTH_LONG).show()
            }
            password.text.toString() == "" -> {
                Toast.makeText(this@LogInActivity, getString(R.string.writepassword), Toast.LENGTH_LONG).show()
            }
            else -> {
                mAuth.signInWithEmailAndPassword(email.text.toString(), password.text.toString()).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val intent = Intent(this@LogInActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this@LogInActivity, getString(R.string.errormessage) + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}