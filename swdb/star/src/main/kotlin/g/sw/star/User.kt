package g.sw.star

import g.sw.simpledb.Table
import g.sw.simpledb.lines.User as UserRecord
import java.io.File
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class User {
    private val table by lazy {
        if (File("users.gsdb").exists()) {
            Table(UserRecord::class.java, "users")
        } else {
            Table(UserRecord::class.java, "users").apply { init() }
        }
    }

    fun register(login: String, password: String, name: String, gender: String = "", birthday: String = ""): String {
        if (login.isBlank()) throw IllegalArgumentException("Login cannot be empty")
        if (password.isBlank()) throw IllegalArgumentException("Password cannot be empty")
        if (name.isBlank()) throw IllegalArgumentException("Name cannot be empty")

        val existing = table.search { it.login == login }
        if (existing.isNotEmpty()) throw IllegalArgumentException("Login already exists: $login")

        val count = table.search { true }.size
        val user = UserRecord(count + 1, login, name, hashPassword(password), "", gender, birthday, "", "", System.currentTimeMillis())
        table.add(user)
        return "ok"
    }

    fun login(login: String, password: String): String {
        val users = table.search { it.login == login }
        if (users.isEmpty()) throw IllegalArgumentException("User not found: $login")

        val user = users.first()
        if (!verifyPassword(password, user.password)) {
            throw IllegalArgumentException("Invalid password")
        }

        val token = UUID.randomUUID().toString().replace("-", "")
        Session.tokens[token] = user.id
        return token
    }

    private fun hashPassword(password: String): String {
        val salt = SecureRandom().generateSeed(16)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(PBEKeySpec(password.toCharArray(), salt, 65536, 256)).encoded
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash)
    }

    private fun verifyPassword(password: String, stored: String): Boolean {
        val parts = stored.split(":")
        val salt = Base64.getDecoder().decode(parts[0])
        val storedHash = Base64.getDecoder().decode(parts[1])
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val inputHash = factory.generateSecret(PBEKeySpec(password.toCharArray(), salt, 65536, 256)).encoded
        return storedHash.contentEquals(inputHash)
    }
}

object Session {
    val tokens = mutableMapOf<String, Int>()
}
