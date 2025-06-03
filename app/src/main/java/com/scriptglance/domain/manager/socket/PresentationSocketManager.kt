package com.scriptglance.domain.manager.socket

import android.util.Log
import com.scriptglance.data.model.PresentationEvent
import com.scriptglance.domain.repository.AuthRepository
import io.socket.emitter.Emitter
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresentationSocketManager @Inject constructor(
    authRepository: AuthRepository
) : BaseSocketManager(authRepository, SOCKET_PATH) {

    override val TAG: String = "PresentationSocketManager"

    companion object {
        private const val SOCKET_PATH = "/presentations"
        private const val EVENT_PRESENTATION_EVENT = "presentationEvent"
        private const val EVENT_SUBSCRIBE_PRESENTATION = "subscribe_presentation"
    }

    fun subscribePresentation(presentationId: Int) {
        val data = JSONObject().apply {
            put("presentationId", presentationId)
        }
        emitEvent(EVENT_SUBSCRIBE_PRESENTATION, data)
    }

    fun onPresentationEvent(callback: (event: PresentationEvent) -> Unit): Emitter.Listener {
        return onEvent(EVENT_PRESENTATION_EVENT, PresentationEvent::class.java, callback)
    }

    fun offPresentationEvent(listener: Emitter.Listener?) {
        offEvent(EVENT_PRESENTATION_EVENT, listener)
    }

    fun offAllPresentationEvents() {
        offAllForEvent(EVENT_PRESENTATION_EVENT)
    }
}