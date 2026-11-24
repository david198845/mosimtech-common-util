package de.mosimtech.common.delegation.access

/**
 * Thread-gebundener Träger des geprüften [DelegationContext].
 *
 * Bewusst ein einfaches [ThreadLocal] und kein `InheritableThreadLocal`: Der Kontext darf nicht
 * in asynchron gestartete Verarbeitung wandern. Der Kontext gilt nur im Request-Thread: In asynchroner
 * Verarbeitung (`@Async`, Executor, geplante Jobs) liefert `CurrentUserProvider.getEffectiveUserId()` mit
 * weitergereichtem Security-Kontext den Aufrufer, ohne Security-Kontext wirft es eine `IllegalStateException`;
 * in beiden Fällen nie den Eigentümer. Wer dort für den Eigentümer arbeiten muss, gibt dessen ID ausdrücklich
 * im Event oder Parameter mit.
 */
object DelegationAccessHolder {
    private val current = ThreadLocal<DelegationContext?>()

    fun set(context: DelegationContext) = current.set(context)

    fun get(): DelegationContext? = current.get()

    fun clear() = current.remove()
}
