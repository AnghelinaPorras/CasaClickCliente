package com.casaclick.cliente.util
import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.casaclick.cliente.data.remote.dto.UsuarioDto
import java.security.SecureRandom
import java.security.MessageDigest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
class SessionStore(context: Context, archivo: String = "casaclick_seguro") {
    private val key = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    private val prefs = EncryptedSharedPreferences.create(context, archivo, key, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    var baseUrl: String
        get() = prefs.getString("url", "http://10.0.2.2:3001/")!!
        set(v) { prefs.edit().putString("url", v).commit() }
    val token: String? get() = prefs.getString("token", null)
    val perfil: UsuarioDto? get() = prefs.getString("perfil", null)?.let { Gson().fromJson(it, UsuarioDto::class.java) }
    val activa: Boolean get() = prefs.getBoolean("activa", false)
    fun guardar(usuario: UsuarioDto, token: String, clave: String, url: String) {
        val salt = ByteArray(24).also { SecureRandom().nextBytes(it) }
        val algoritmo = if(android.os.Build.VERSION.SDK_INT >= 26) "PBKDF2WithHmacSHA256" else "PBKDF2WithHmacSHA1"
        prefs.edit().putString("perfil", Gson().toJson(usuario)).putString("token", token).putString("salt", b64(salt)).putString("algoritmo",algoritmo).putString("hash", b64(hash(clave, salt, algoritmo))).putString("url", url).putBoolean("activa", true).commit()
    }
    fun verificarOffline(correo: String, clave: String, url: String): Boolean {
        if (perfil?.correo != correo || baseUrl != url) return false
        val salt = prefs.getString("salt", null) ?: return false
        val esperado = prefs.getString("hash", null) ?: return false
        val algoritmo = prefs.getString("algoritmo","PBKDF2WithHmacSHA256")!!
        return MessageDigest.isEqual(Base64.decode(esperado, 0), hash(clave, Base64.decode(salt, 0), algoritmo))
    }
    fun activar() { prefs.edit().putBoolean("activa", true).commit() }
    fun salir() { prefs.edit().putBoolean("activa", false).commit() }
    private fun b64(b: ByteArray) = Base64.encodeToString(b, Base64.NO_WRAP)
    private fun hash(clave: String, salt: ByteArray, algoritmo: String): ByteArray {
        val spec = PBEKeySpec(clave.toCharArray(), salt, 120000, 256)
        return try { SecretKeyFactory.getInstance(algoritmo).generateSecret(spec).encoded } finally { spec.clearPassword() }
    }
}
