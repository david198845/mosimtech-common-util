package de.modulix.mosimtech.dto

import java.io.Serializable
import java.time.LocalDateTime

data class DocumentDTO(
    val id: String?,
    val creationDate: LocalDateTime?,
    val createdBy: String?,
    val lastModifiedBy: String? = null,
    val lastModifiedDate: LocalDateTime? = null,
    val userId: String,
    val version: Long,
    val valid: Boolean = true,
    val externalUrn: String,
    val filename: String,
    val contentType: String,
    val fileSize: Long,
    val content: ByteArray? = null,
    val contentBase64: String? = null,
): Serializable {
    // Sekundärer Konstruktor für optionale Felder
    constructor(
        userId: String,
        externalUrn: String,
        filename: String,
        contentType: String,
        fileSize: Long,
        content: ByteArray? = null,
        contentBase64: String? = null,
    ) : this(
        id = null,
        creationDate = null,
        createdBy = null,
        lastModifiedBy = null,
        lastModifiedDate = null,
        userId = userId,
        version = 0,
        valid = true,
        externalUrn = externalUrn,
        filename = filename,
        contentType = contentType,
        fileSize = fileSize,
        content = content,
        contentBase64 = contentBase64,
    )
}