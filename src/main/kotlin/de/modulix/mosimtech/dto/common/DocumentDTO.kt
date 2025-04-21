package de.modulix.mosimtech.dto.common

import java.io.Serializable
import java.time.ZonedDateTime

data class DocumentDTO(
    val id: String?,
    val creationDate: ZonedDateTime?,
    val createdBy: String?,
    val lastModifiedBy: String? = null,
    val lastModifiedDate: ZonedDateTime? = null,
    val userId: String,
    val userToken: String? = null,
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
        userToken: String? = null,
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
        userToken = userToken,
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

    override fun toString(): String {
        return "DocumentDTO(id=$id, creationDate=$creationDate, createdBy=$createdBy, " +
                "lastModifiedBy=$lastModifiedBy, lastModifiedDate=$lastModifiedDate, " +
                "userId=$userId, userToken=$userToken, version=$version, valid=$valid, " +
                "externalUrn=$externalUrn, filename=$filename, contentType=$contentType, " +
                "fileSize=$fileSize, content=${content?.contentToString()}, " +
                "contentBase64=$contentBase64, moduleName=$moduleName)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentDTO) return false

        if (id != other.id) return false
        if (creationDate != other.creationDate) return false
        if (createdBy != other.createdBy) return false
        if (lastModifiedBy != other.lastModifiedBy) return false
        if (lastModifiedDate != other.lastModifiedDate) return false
        if (userId != other.userId) return false
        if (userToken != other.userToken) return false
        if (version != other.version) return false
        if (valid != other.valid) return false
        if (externalUrn != other.externalUrn) return false
        if (filename != other.filename) return false
        if (contentType != other.contentType) return false
        if (fileSize != other.fileSize) return false
        if (content != null) {
            if (other.content == null) return false
            if (!content.contentEquals(other.content)) return false
        } else if (other.content != null) return false
        if (contentBase64 != other.contentBase64) return false
        if (moduleName != other.moduleName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (creationDate?.hashCode() ?: 0)
        result = 31 * result + (createdBy?.hashCode() ?: 0)
        result = 31 * result + (lastModifiedBy?.hashCode() ?: 0)
        result = 31 * result + (lastModifiedDate?.hashCode() ?: 0)
        result = 31 * result + userId.hashCode()
        result = 31 * result + (userToken?.hashCode() ?: 0)
        result = 31 * result + version.hashCode()
        result = 31 * result + valid.hashCode()
        result = 31 * result + externalUrn.hashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + (content?.contentHashCode() ?: 0)
        result = 31 * result + (contentBase64?.hashCode() ?: 0)
        result = 31 * result + moduleName.hashCode()
        return result
    }
}