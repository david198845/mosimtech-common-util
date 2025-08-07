package de.mosimtech.common.core.dto.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.mosimtech.common.core.dto.base.BaseDTO
import java.time.LocalDateTime

/**
 * Data Transfer Object (DTO) that represents a document in the system.
 * This class holds information about the document's metadata and content.
 *
 * Implements the `BaseDTO` interface to include common properties such as
 * identifiers, creation and modification metadata, user information, and
 * validity status.
 *
 * Primary constructor includes all properties necessary for full representation
 * of the document, while a secondary constructor is provided for flexibility
 * when initializing the object with optional metadata fields.
 *
 * Properties:
 * - id: Unique identifier of the document.
 * - creationDate: Timestamp when the document was created.
 * - createdBy: Identifier of the user or process that created the document.
 * - lastModifiedBy: Identifier of the user or process that last modified the document.
 * - lastModifiedDate: Timestamp of the last modification.
 * - userId: Identifier of the user associated with the document.
 * - userToken: Optional token for user-level authorization or identification.
 * - version: Versioning information for the document to support concurrency control.
 * - valid: Boolean flag indicating whether the document is currently valid.
 * - externalUrn: Uniform Resource Name (URN) serving as an external, unique reference for the document.
 * - filename: Name of the file associated with the document.
 * - contentType: MIME notificationType representing the content notificationType of the document.
 * - fileSize: Size of the document's content in bytes.
 * - content: Binary content of the document as a byte array.
 * - contentBase64: Base64-encoded representation of the document content, optional.
 * - moduleName: Application module or context name associated with this document.
 *
 * Functions:
 * - toString: Returns a string representation of the document, including its metadata and properties.
 * - equals: Compares two `DocumentDTO` objects based on their properties for equality.
 * - hashCode: Generates a hash code for the document based on its properties.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DocumentDTO(
    override val id: String?,
    override val creationDate: LocalDateTime?,
    override val createdBy: String?,
    override val lastModifiedBy: String? = null,
    override val lastModifiedDate: LocalDateTime? = null,
    override val userId: String,
    val userToken: String? = null,
    override val version: Long,
    override val valid: Boolean = true,
    val externalUrn: String,
    val filename: String,
    val contentType: String,
    val fileSize: Long,
    val content: ByteArray? = null,
    val contentBase64: String? = null,
    val moduleName: String
): BaseDTO {
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
