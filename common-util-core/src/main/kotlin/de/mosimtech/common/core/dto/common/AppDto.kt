package de.mosimtech.common.core.dto.common

data class AppDto(
    val key: String?,  // toolKey, z.B. "drive"
    val label: String?,
    val url: String?,
    val icon: String?,
    val order: Int?
)