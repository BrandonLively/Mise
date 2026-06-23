package com.patchfox.mise.ui.state

/**
 * The two cook-day stages. The old Phase 0 (long-running kickoffs) is folded into
 * [COOK] as the leading `backgroundable` cook steps — there is no separate kickoff
 * stage in v3.
 */
enum class CookStage { MISE, COOK }
