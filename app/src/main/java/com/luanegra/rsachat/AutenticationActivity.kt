package com.luanegra.rsachat

import android.app.KeyguardManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor


class AutenticationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autentication)
        ActType = intent.getStringExtra("activityType")
        reciever_id = intent.getStringExtra("reciever_id").toString()
        reciever_profile = intent.getStringExtra("reciever_profile").toString()
        reciever_username = intent.getStringExtra("reciever_username").toString()
        showBiomertricDialog()
    }

    lateinit var executor: Executor
    lateinit var biometricPrompt: BiometricPrompt
    lateinit var promptInfo: BiometricPrompt.PromptInfo
    private val CODE_AUTHENTICATION_VERIFICATION = 241
    var ActType: String? = ""
    var reciever_id: String? = ""
    var reciever_profile: String? = ""
    var reciever_username: String? = ""
    fun showBiomertricDialog(){
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    if(ActType == "notification"){
                        val intent = Intent(this@AutenticationActivity, MessageChatActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra("resultAUTH", "true")
                        intent.putExtra("reciever_id", reciever_id)
                        intent.putExtra("reciever_profile", reciever_profile)
                        intent.putExtra("reciever_username", reciever_username)
                        startActivity(intent)
                        finish()
                    }else{
                        val intent = Intent(this@AutenticationActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra("resultAUTH", "true")
                        startActivity(intent)
                        finish()
                    }

                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                    if (km.isKeyguardSecure) {
                        val i = km.createConfirmDeviceCredentialIntent(
                            getString(R.string.authenticationrequired),
                            "password"
                        )
                        startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION)
                    } else {
                        if(ActType == "notification"){
                            val intent = Intent(this@AutenticationActivity, MessageChatActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.putExtra("resultAUTH", "true")
                            intent.putExtra("reciever_id", reciever_id)
                            intent.putExtra("reciever_profile", reciever_profile)
                            intent.putExtra("reciever_username", reciever_username)
                            startActivity(intent)
                            finish()
                        }else{
                            val intent = Intent(this@AutenticationActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.putExtra("resultAUTH", "true")
                            startActivity(intent)
                            finish()
                        }
                    }
                }

            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.fingerprintlogin))
            .setSubtitle(getString(R.string.loginusingfingerprint))
            .setDeviceCredentialAllowed(true)
            .build()
        biometricPrompt.authenticate(promptInfo)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == CODE_AUTHENTICATION_VERIFICATION) {
            if(ActType == "notification"){
                val intent = Intent(this@AutenticationActivity, MessageChatActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("resultAUTH", "true")
                intent.putExtra("reciever_id", reciever_id)
                intent.putExtra("reciever_profile", reciever_profile)
                intent.putExtra("reciever_username", reciever_username)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this@AutenticationActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("resultAUTH", "true")
                startActivity(intent)
                finish()
            }
        }
    }
}