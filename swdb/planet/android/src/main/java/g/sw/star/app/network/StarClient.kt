package g.sw.star.app.network

import android.content.Context
import g.sw.star.app.StarApp
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class StarClient(context: Context? = null) {
    private val baseUrl: String = (context?.applicationContext as? StarApp)?.serverUrl
        ?: "http://10.0.2.2:8081"

    fun call(handler: String, method: String, args: Map<String, String> = emptyMap()): String {
        val url = URL("$baseUrl/$handler/$method")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

        val body = args.entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
        }
        OutputStreamWriter(conn.outputStream).use { it.write(body) }

        val code = conn.responseCode
        val reader = if (code in 200..299) {
            BufferedReader(InputStreamReader(conn.inputStream))
        } else {
            BufferedReader(InputStreamReader(conn.errorStream))
        }
        return reader.readText().also { conn.disconnect() }
    }
}
