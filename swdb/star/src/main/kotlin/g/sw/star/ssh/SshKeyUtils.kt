package g.sw.star.ssh

import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.Base64

object SshKeyUtils {
    fun parsePublicKey(keyLine: String): PublicKey? {
        val parts = keyLine.trim().split("\\s+".toRegex())
        if (parts.size < 2) return null
        val data = Base64.getDecoder().decode(parts[1])
        return when (parts[0]) {
            "ssh-rsa" -> parseRsa(ByteArrayInputStream(data))
            else -> null
        }
    }

    private fun parseRsa(bis: ByteArrayInputStream): PublicKey {
        readString(bis)
        val e = readBigInt(bis)
        val n = readBigInt(bis)
        return KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(n, e))
    }

    fun keyEquals(keyLine: String, publicKey: PublicKey): Boolean {
        val parsed = parsePublicKey(keyLine) ?: return false
        return parsed.encoded.contentEquals(publicKey.encoded)
    }

    private fun readString(bis: ByteArrayInputStream): String {
        val buf = ByteArray(readInt(bis))
        bis.read(buf)
        return String(buf, Charsets.UTF_8)
    }

    private fun readBigInt(bis: ByteArrayInputStream): BigInteger {
        val buf = ByteArray(readInt(bis))
        bis.read(buf)
        return BigInteger(buf)
    }

    private fun readInt(bis: ByteArrayInputStream): Int {
        return (bis.read() shl 24) or (bis.read() shl 16) or (bis.read() shl 8) or bis.read()
    }
}
