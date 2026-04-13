package de.mosimtech.common.delegation

object DelegationContextHolder {
    private val targetUserId = ThreadLocal<String?>()

    fun set(userId: String) = targetUserId.set(userId)
    fun get(): String? = targetUserId.get()
    fun clear() = targetUserId.remove()
}
