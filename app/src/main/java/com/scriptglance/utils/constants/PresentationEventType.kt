package com.scriptglance.utils.constants

enum class PresentationEventType(val eventType: String) {
    NAME_CHANGED("name_changed"),
    PARTICIPANTS_CHANGED("participants_changed"),
    TEXT_CHANGED("text_changed"),
    PRESENTATION_STARTED("presentation_started"),
    PRESENTATION_STOPPED("presentation_stopped"),
    JOINED_USERS_CHANGED("joined_users_changed");

    companion object {
        fun fromEvent(name: String): PresentationEventType? {
            return entries.find { it.eventType == name }
        }
    }
}