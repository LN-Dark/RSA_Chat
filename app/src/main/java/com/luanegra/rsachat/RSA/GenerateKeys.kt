package com.luanegra.rsachat.RSA

import android.util.Base64
import java.security.KeyPairGenerator

class GenerateKeys {

    fun generateKeys(): List<String> {
        val keys = ArrayList<String>()
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)
        val kp = kpg.generateKeyPair()
        keys.add("-----BEGIN PUBLIC KEY-----\n" + Base64.encodeToString(kp.public.encoded, Base64.DEFAULT) + "\n-----END PUBLIC KEY-----")
        keys.add("-----BEGIN PRIVATE KEY-----\n" + Base64.encodeToString(kp.private.encoded, Base64.DEFAULT) + "\n-----END PRIVATE KEY-----")
        return keys
    }
}