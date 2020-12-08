package com.luanegra.rsachat.RSA

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

class PublicKeyReaderStorage {
    companion object {
        @JvmStatic
        fun get(fileName: String? = null): RSAPublicKey? {
            try {
                val keyBytes = Files.readAllBytes(Paths.get(fileName))
                val publicKeyReplace = keyBytes.toString(Charset.forName("UTF-8")).replace("\\n".toRegex(), "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")
                val kf = KeyFactory.getInstance("RSA")
                val keySpecX509 = X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyReplace))
                return kf.generatePublic(keySpecX509) as RSAPublicKey
            } catch (ex: NoSuchAlgorithmException) {
                ex.printStackTrace()
            } catch (ex: InvalidKeySpecException) {
                ex.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return null
        }
    }
}