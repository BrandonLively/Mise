package com.patchfox.mise.data.cookcard

/**
 * Abstraction over the wire source for the cook card. Implementations push the latest
 * payload into the local cache; the repository observes the cache and republishes.
 */
interface CookCardSource {
    /**
     * Attach a snapshot listener for the cook-card collection. Calls [onUpdate] with the
     * newest cook card's payload (or an error string on failure). Replaces any previous
     * listener.
     */
    fun startListening(onUpdate: (payload: String?, error: String?) -> Unit)

    fun stopListening()

    /** One-shot fetch (e.g. for manual refresh). */
    suspend fun refreshOnce()
}
