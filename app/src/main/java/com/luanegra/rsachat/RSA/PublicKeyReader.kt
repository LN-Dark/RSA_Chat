package com.luanegra.rsachat.RSA

import java.security.spec.InvalidKeySpecException
import java.security.NoSuchAlgorithmException
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

class PublicKeyReader {
    companion object {
        @JvmStatic
        fun get(publicKey: String? = null): RSAPublicKey? {
            var publicKeyContent = publicKey
            try {
                publicKeyContent = publicKeyContent?.replace("\\n".toRegex(), "")?.replace("-----BEGIN PUBLIC KEY-----", "")?.replace("-----END PUBLIC KEY-----", "")
                val kf = KeyFactory.getInstance("RSA")
                val keySpecX509 = X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent))
                return kf.generatePublic(keySpecX509) as RSAPublicKey
            } catch (ex: NoSuchAlgorithmException) {
                ex.printStackTrace()
            } catch (ex: InvalidKeySpecException) {
                ex.printStackTrace()
            }
            return null
        }
    }
}