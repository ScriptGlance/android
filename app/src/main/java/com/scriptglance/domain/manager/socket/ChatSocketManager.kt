package com.scriptglance.domain.manager.socket

import com.scriptglance.data.model.chat.NewMessageEvent
import com.scriptglance.domain.repository.AuthRepository
import io.socket.emitter.Emitter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatSocketManager @Inject constructor(
    authRepository: AuthRepository
) : BaseSocketManager(authRepository, SOCKET_PATH) {

    override val TAG: String = "ChatSocketManager"

    companion object {
        private const val SOCKET_PATH = "/chats"
        private const val EVENT_JOIN_USER_CHAT = "join_user_chat"
        private const val EVENT_NEW_MESSAGE = "new_message"
        private const val EVENT_CHAT_CLOSED = "chat_closed"
    }

    fun joinUserChat() {
        emitEvent(EVENT_JOIN_USER_CHAT)
    }

    fun onNewMessage(callback: (event: NewMessageEvent) -> Unit): Emitter.Listener {
        return onEvent(EVENT_NEW_MESSAGE, NewMessageEvent::class.java, callback)
    }

    fun offNewMessage(listener: Emitter.Listener?) {
        offEvent(EVENT_NEW_MESSAGE, listener)
    }

    fun onChatClosed(callback: () -> Unit): Emitter.Listener {
        return onEmptyEvent(EVENT_CHAT_CLOSED) { callback() }
    }

    fun offChatClosed(listener: Emitter.Listener?) {
        offEvent(EVENT_CHAT_CLOSED, listener)
    }
}