package g.sw.star

import g.sw.simpledb.Table
import g.sw.simpledb.lines.Attachment as AttachmentRecord
import java.io.File
import java.util.Base64
import java.util.UUID

class Attachment {
    private val table by lazy {
        if (File("attachments.gsdb").exists()) {
            Table(AttachmentRecord::class.java, "attachments")
        } else {
            Table(AttachmentRecord::class.java, "attachments").apply { init() }
        }
    }

    private val filesDir = File("attachments").also { it.mkdirs() }

    private fun index(id: Int): Long {
        val idx = table.searchIndex { it.id == id }
        if (idx == -1L) throw IllegalArgumentException("Attachment not found: $id")
        return idx
    }

    fun add(entityType: String, entityId: Int, fileName: String, data: String): String {
        if (entityType.isBlank()) throw IllegalArgumentException("entityType cannot be empty")
        if (fileName.isBlank()) throw IllegalArgumentException("fileName cannot be empty")
        if (data.isBlank()) throw IllegalArgumentException("data cannot be empty")

        val bytes = Base64.getDecoder().decode(data)
        val storedName = "${UUID.randomUUID()}_$fileName"
        val file = File(filesDir, storedName)
        file.writeBytes(bytes)

        val id = table.search { true }.size + 1
        table.add(AttachmentRecord(id, entityType, entityId, fileName, file.absolutePath,
            detectMimeType(fileName), bytes.size.toLong(), System.currentTimeMillis()))
        return "ok"
    }

    fun list(entityType: String, entityId: Int): List<AttachmentRecord> {
        return table.search { it.entityType == entityType && it.entityId == entityId }
    }

    fun get(id: Int): AttachmentRecord? {
        val idx = table.searchIndex { it.id == id }
        return if (idx == -1L) null else table[idx]
    }

    fun getData(id: Int): String {
        val att = get(id) ?: throw IllegalArgumentException("Attachment not found: $id")
        val file = File(att.filePath)
        if (!file.exists()) throw IllegalArgumentException("File not found on disk")
        return Base64.getEncoder().encodeToString(file.readBytes())
    }

    fun delete(id: Int): String {
        val att = get(id) ?: throw IllegalArgumentException("Attachment not found: $id")
        File(att.filePath).delete()
        table.remove(index(id))
        return "ok"
    }

    private fun detectMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "svg" -> "image/svg+xml"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }
}
