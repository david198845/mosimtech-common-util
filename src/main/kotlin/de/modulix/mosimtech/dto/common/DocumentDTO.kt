package de.modulix.mosimtech.dto.common

import java.io.Serializable
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class DocumentDTO(
    val id: String?,
    val creationDate: ZonedDateTime?,
    val createdBy: String?,
    val lastModifiedBy: String? = null,
    val lastModifiedDate: ZonedDateTime? = null,
    val userId: String,
    val version: Long,
    val valid: Boolean = true,
    val externalUrn: String,
    val filename: String,
    val contentType: String,
    val fileSize: Long,
    val content: ByteArray? = null,
    val contentBase64: String? = null,
    val moduleName: String
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
        moduleName: String
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
        moduleName = moduleName
    )
}