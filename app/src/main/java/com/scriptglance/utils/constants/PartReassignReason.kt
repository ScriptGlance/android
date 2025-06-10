package com.scriptglance.utils.constants

enum class PartReassignReason(val value: String) {
    MISSING_ASSIGNEE("missing_assignee"),
    ASSIGNEE_NOT_RESPONDING("assignee_not_responding");

    companion object {
        fun fromValue(value: String): PartReassignReason? {
            return PartReassignReason.entries.find { it.value == value }
        }
    }
}