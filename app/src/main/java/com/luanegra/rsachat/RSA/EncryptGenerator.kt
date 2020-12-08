package com.luanegra.rsachat.RSA

import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSAEncrypter
import java.io.File
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException

class EncryptGenerator {
    companion object {
        @JvmStatic
        fun generateEncrypt(plainText: String? = null, publicKey: String? = null, jWEAlgorithm: String? = null, encryptionMethod: String? = null): String? {
            try {
                val header = JWEHeader(JWEAlgorithm.parse(jWEAlgorithm), EncryptionMethod.parse(encryptionMethod))
                val payload = Payload(plainText)
                val jweObject = JWEObject(header, payload)
                val publicKeyReader = PublicKeyReader.get(publicKey)
                val encrypted = RSAEncrypter(publicKeyReader)
                jweObject.encrypt(encrypted)
                return jweObject.serialize()
            } catch (ex: NoSuchAlgorithmException) {
                ex.printStackTrace()
            } catch (ex: InvalidKeySpecException) {
                ex.printStackTrace()
            } catch (ex: JOSEException) {
                ex.printStackTrace()
            }
            return null
        }

        @JvmStatic
        fun generateEncrypt(plainText: String? = null, path: String? = null, fileName: String? = null, jWEAlgorithm: String? = null, encryptionMethod: String? = null): String? {
            try {
                val header = JWEHeader(JWEAlgorithm.parse(jWEAlgorithm), EncryptionMethod.parse(encryptionMethod))
                val payload = Payload(plainText)
                val jweObject = JWEObject(header, payload)
                val pathSeparator = if (path?.isEndSeparator() == false) "$path${File.separator}$fileName" else "$path$fileName"
                val publicKeyReader = PublicKeyReaderStorage.get(pathSeparator)
                val encrypted = RSAEncrypter(publicKeyReader)
                jweObject.encrypt(encrypted)
                return jweObject.serialize()
            } catch (ex: NoSuchAlgorithmException) {
                ex.printStackTrace()
            } catch (ex: InvalidKeySpecException) {
                ex.printStackTrace()
            } catch (ex: JOSEException) {
                ex.printStackTrace()
            }
            return null
        }
    }
}