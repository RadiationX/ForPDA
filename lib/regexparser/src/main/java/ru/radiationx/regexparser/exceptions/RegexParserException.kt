package ru.radiationx.regexparser.exceptions

import ru.radiationx.regexparser.core.RegexContext

class RegexParserException(cause: Throwable? = null) : Exception(cause) {

    private val breadcrumbs = BreadCrumbs()

    private val completed = mutableListOf<String>()

    internal fun setRequiredGroup(group: Int) {
        breadcrumbs.setRequiredGroup(group)
    }

    internal fun setRequiredMatch() {
        breadcrumbs.setRequiredMatch()
    }

    internal fun setFinderState(lastMatchIndex: Int, lastUsedGroup: Int) {
        breadcrumbs.setFinderState(lastMatchIndex, lastUsedGroup)
    }

    internal fun setContext(context: RegexContext) {
        breadcrumbs.setContext(context)
    }

    internal fun complete() {
        completed.add(breadcrumbs.complete())
    }

    override val message: String
        get() {
            val completedMessage = completed.joinToString(" -> ")
            val causeMessage = cause?.let { "(${it.toString()})" }.orEmpty()
            return "$causeMessage$completedMessage"
        }

    private class BreadCrumbs {
        private var _requiredGroup: Int? = null
        private var _requiredMatch: Boolean = false
        private var _lastMatchIndex: Int? = null
        private var _lastUsedGroup: Int? = null
        private var _context: RegexContext? = null

        fun setRequiredGroup(group: Int) {
            _requiredGroup = group
        }

        fun setRequiredMatch() {
            _requiredMatch = true
        }

        fun setFinderState(lastMatchIndex: Int, lastUsedGroup: Int) {
            _lastMatchIndex = lastMatchIndex
            _lastUsedGroup = lastUsedGroup
        }

        fun setContext(context: RegexContext) {
            _context = context
        }

        fun complete(): String {
            val result = buildString {
                append("[C:${_context?.message}]")
                append("[SS:$_lastMatchIndex:$_lastUsedGroup]")
                if (_requiredMatch) {
                    append("[RM]")
                }
                _requiredGroup?.also {
                    append("[RG:$it]")
                }
            }
            _requiredGroup = null
            _requiredMatch = false
            _lastMatchIndex = null
            _lastUsedGroup = null
            _context = null
            return result
        }
    }
}