package com.scriptglance.domain.manager.socket

import android.util.Log
import com.scriptglance.data.model.teleprompter.*
import com.scriptglance.domain.repository.AuthRepository
import io.socket.emitter.Emitter
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeleprompterSocketManager @Inject constructor(
    authRepository: AuthRepository
) : BaseSocketManager(authRepository, SOCKET_PATH) {

    override val TAG: String = "TeleprompterSocketManager"

    companion object {
        private const val SOCKET_PATH = "/teleprompter"
        private const val EVENT_SUBSCRIBE_TELEPROMPTER = "subscribe_teleprompter"
        private const val EVENT_READING_POSITION = "reading_position"
        private const val EVENT_TELEPROMPTER_PRESENCE = "teleprompter_presence"
        private const val EVENT_OWNER_CHANGED = "owner_changed"
        private const val EVENT_PART_REASSIGN_REQUIRED = "part_reassign_required"
        private const val EVENT_PART_REASSIGN_CANCELLED = "part_reassign_cancelled"
        private const val EVENT_WAITING_FOR_USER = "waiting_for_user"
        private const val EVENT_PART_READING_CONFIRMATION_REQUIRED = "part_reading_confirmation_required"
        private const val EVENT_PART_READING_CONFIRMATION_CANCELLED = "part_reading_confirmation_cancelled"
        private const val EVENT_PART_REASSIGNED = "part_reassigned"
        private const val RECORDING_MODE_CHANGED = "recording_mode_changed"

        private const val EVENT_DISCONNECT = "disconnect"
        private const val EVENT_RECONNECT_ATTEMPT = "reconnect_attempt"
        private const val EVENT_RECONNECT = "reconnect"
        private const val EVENT_RECONNECT_FAILED = "reconnect_failed"
    }
    

    fun subscribeToTeleprompter(presentationId: Int) {
        val payload = JSONObject().apply {
            put("presentationId", presentationId)
        }
        emitEvent(EVENT_SUBSCRIBE_TELEPROMPTER, payload)
    }

    fun sendReadingPosition(position: Int, presentationId: Int) {
        val payload = JSONObject().apply {
            put("position", position)
            put("presentationId", presentationId)
        }
        emitEvent(EVENT_READING_POSITION, payload)
    }

    fun onTeleprompterPresence(callback: (data: TeleprompterPresencePayload) -> Unit): Emitter.Listener {
        return onEvent(EVENT_TELEPROMPTER_PRESENCE, TeleprompterPresencePayload::class.java, callback)
    }
    
    fun offTeleprompterPresence(listener: Emitter.Listener?) {
        offEvent(EVENT_TELEPROMPTER_PRESENCE, listener)
    }

    fun onOwnerChanged(callback: (data: OwnerChangedPayload) -> Unit): Emitter.Listener {
        return onEvent(EVENT_OWNER_CHANGED, OwnerChangedPayload::class.java, callback)
    }
    
    fun offOwnerChanged(listener: Emitter.Listener?) {
        offEvent(EVENT_OWNER_CHANGED, listener)
    }

    fun onRecordingModeChanged(callback: (data: RecordingModeChangedPayload) -> Unit): Emitter.Listener {
        return onEvent(RECORDING_MODE_CHANGED, RecordingModeChangedPayload::class.java, callback)
    }

    fun offRecordingModeChanged(listener: Emitter.Listener?) {
        offEvent(RECORDING_MODE_CHANGED, listener)
    }

    fun onPartReassignRequired(callback: (data: PartReassignRequiredPayload) -> Unit): Emitter.Listener {
        return onEvent(EVENT_PART_REASSIGN_REQUIRED, PartReassignRequiredPayload::class.java, callback)
    }

    fun offPartReassignRequired(listener: Emitter.Listener?) {
        offEvent(EVENT_PART_REASSIGN_REQUIRED, listener)
    }

    fun onPartReassignCancelled(callback: () -> Unit): Emitter.Listener {
        return onEmptyEvent(EVENT_PART_REASSIGN_CANCELLED, callback)
    }
    
    fun offPartReassignCancelled(listener: Emitter.Listener?) {
        offEvent(EVENT_PART_REASSIGN_CANCELLED, listener)
    }


    fun onWaitingForUser(callback: (data: WaitingForUserPayload) -> Unit): Emitter.Listener {
        return onEvent(EVENT_WAITING_FOR_USER, WaitingForUserPayload::class.java, callback)
    }
    
    fun offWaitingForUser(listener: Emitter.Listener?) {
        offEvent(EVENT_WAITING_FOR_USER, listener)
    }

    fun onPartReadingConfirmationRequired(callback: (data: PartReadingConfirmationRequiredPayload) -> Unit): Emitter.Listener {
        return onEvent(EVENT_PART_READING_CONFIRMATION_REQUIRED, PartReadingConfirmationRequiredPayload::class.java, callback)
    }
    
    fun offPartReadingConfirmationRequired(listener: Emitter.Listener?) {
        offEvent(EVENT_PART_READING_CONFIRMATION_REQUIRED, listener)
    }


    fun onPartReadingConfirmationCancelled(callback: () -> Unit): Emitter.Listener {
        return onEmptyEvent(EVENT_PART_READING_CONFIRMATION_CANCELLED, callback)
    }
    
    fun offPartReadingConfirmationCancelled(listener: Emitter.Listener?) {
        offEvent(EVENT_PART_READING_CONFIRMATION_CANCELLED, listener)
    }


    fun onReadingPositionChanged(callback: (data: IncomingReadingPositionPayload) -> Unit): Emitter.Listener {
        return onEvent(EVENT_READING_POSITION, IncomingReadingPositionPayload::class.java, callback)
    }
    
    fun offReadingPositionChanged(listener: Emitter.Listener?) {
        offEvent(EVENT_READING_POSITION, listener)
    }


    fun onPartReassigned(callback: (data: PartReassignedPayload) -> Unit): Emitter.Listener {
        return onEvent(EVENT_PART_REASSIGNED, PartReassignedPayload::class.java, callback)
    }
    
    fun offPartReassigned(listener: Emitter.Listener?) {
        offEvent(EVENT_PART_REASSIGNED, listener)
    }

    fun onDisconnect(callback: (reason: String?) -> Unit): Emitter.Listener {
        val listener = Emitter.Listener { args ->
            val reason = if (args.isNotEmpty() && args[0] != null) args[0].toString() else null
            callback(reason)
        }
        socket?.on(EVENT_DISCONNECT, listener)
        return listener
    }
    
    fun offDisconnect(listener: Emitter.Listener?) {
        offEvent(EVENT_DISCONNECT, listener)
    }


    fun onReconnectAttempt(callback: (attempt: Int) -> Unit): Emitter.Listener {
        val listener = Emitter.Listener { args ->
            val attempt = if (args.isNotEmpty() && args[0] != null) {
                try {
                    args[0].toString().toInt()
                } catch (e: NumberFormatException) {
                    0
                }
            } else 0
            callback(attempt)
        }
        socket?.on(EVENT_RECONNECT_ATTEMPT, listener)
        return listener
    }
    
    fun offReconnectAttempt(listener: Emitter.Listener?) {
        offEvent(EVENT_RECONNECT_ATTEMPT, listener)
    }


    fun onReconnect(callback: () -> Unit): Emitter.Listener {
        return onEmptyEvent(EVENT_RECONNECT, callback)
    }
    
    fun offReconnect(listener: Emitter.Listener?) {
        offEvent(EVENT_RECONNECT, listener)
    }


    fun onReconnectFailed(callback: () -> Unit): Emitter.Listener {
        return onEmptyEvent(EVENT_RECONNECT_FAILED, callback)
    }
    
    fun offReconnectFailed(listener: Emitter.Listener?) {
        offEvent(EVENT_RECONNECT_FAILED, listener)
    }
}