package com.scriptglance.utils.constants

enum class PresenceEventType(val value: String) {
    USER_JOINED("user_joined"),
    
    USER_LEFT("user_left");

    companion object {
        fun fromValue(value: String): PresenceEventType? {
            return PresenceEventType.entries.find { it.value == value }
        }
    }
}

