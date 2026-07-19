package g.sw.star.http

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import g.sw.simpledb.Table
import g.sw.simpledb.lines.Attachment
import java.io.File
import java.net.InetSocketAddress
import java.net.URLDecoder

class StarHttpServer(
    private val port: Int = 8080,
    private val dispatcher: Dispatcher = Dispatcher(),
) {
    private val server = HttpServer.create(InetSocketAddress(port), 0)

    fun start() {
        server.createContext("/") { exchange -> handle(exchange) }
        server.executor = null
        server.start()
        println("HTTP server listening on http://localhost:$port/")
    }

    fun stop() {
        server.stop(0)
    }

    private fun handle(exchange: HttpExchange) {
        try {
            val path = exchange.requestURI.path.trim('/')
            if (path.isEmpty()) {
                respond(exchange, 200, "Star - Family Server")
                return
            }

            if (path.startsWith("Attachment/file/")) {
                serveFile(exchange, path.removePrefix("Attachment/file/"))
                return
            }

            val segments = path.split('/')
            val (className, methodName) = if (segments.size == 1) {
                Pair(dispatcher.resolveClass(segments[0]), "handle")
            } else {
                Pair(dispatcher.resolveClass(segments.dropLast(1).joinToString(".")), segments.last())
            }

            val query = parseQuery(exchange.requestURI.rawQuery)
            val body = exchange.requestBody.readAllBytes().let { bytes ->
                if (bytes.isEmpty()) null else bytes.toString(Charsets.UTF_8)
            }
            val bodyParams = if (body != null) parseQuery(body) else emptyMap()
            val allParams = if (bodyParams.isNotEmpty()) query + bodyParams else query

            val result = dispatcher.dispatch(className, methodName, allParams)
            respond(exchange, 200, result?.toString() ?: "")
        } catch (e: ClassNotFoundException) {
            respond(exchange, 404, "Class not found: ${e.message}")
        } catch (e: NoSuchMethodException) {
            respond(exchange, 404, "Method not found: ${e.message}")
        } catch (e: IllegalArgumentException) {
            respond(exchange, 400, e.message ?: "Bad request")
        } catch (e: Exception) {
            respond(exchange, 500, "${e::class.simpleName}: ${e.cause?.message ?: e.message}")
        } finally {
            exchange.close()
        }
    }

    private fun parseQuery(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrEmpty()) return emptyMap()
        return rawQuery.split('&').mapNotNull { pair ->
            val eq = pair.indexOf('=')
            if (eq == -1) return@mapNotNull null
            val key = URLDecoder.decode(pair.substring(0, eq), "UTF-8")
            val value = URLDecoder.decode(pair.substring(eq + 1), "UTF-8")
            key to value
        }.toMap()
    }

    private fun serveFile(exchange: HttpExchange, idStr: String) {
        try {
            val id = idStr.toInt()
            val table = if (File("attachments.gsdb").exists())
                Table(Attachment::class.java, "attachments") else return respond(exchange, 404, "No DB")
            val idx = table.searchIndex { it.id == id }
            if (idx == -1L) { respond(exchange, 404, "Attachment not found"); return }
            val att = table[idx]
            val file = File(att.filePath)
            if (!file.exists()) { respond(exchange, 404, "File not found on disk"); return }
            val bytes = file.readBytes()
            exchange.responseHeaders.set("Content-Type", att.mimeType)
            exchange.responseHeaders.set("Content-Disposition", "inline; filename=\"${att.fileName}\"")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.write(bytes)
        } catch (_: NumberFormatException) {
            respond(exchange, 400, "Invalid ID")
        }
    }

    private fun respond(exchange: HttpExchange, status: Int, body: String) {
        val bytes = body.toByteArray(Charsets.UTF_8)
        exchange.responseHeaders.set("Content-Type", "text/plain; charset=utf-8")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.write(bytes)
    }
}
