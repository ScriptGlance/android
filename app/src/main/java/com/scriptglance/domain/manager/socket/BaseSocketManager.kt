package com.scriptglance.domain.manager.socket

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.scriptglance.domain.repository.AuthRepository
import com.scriptglance.utils.constants.SOCKET_BASE_URL
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException

abstract class BaseSocketManager(
    private val authRepository: AuthRepository,
    private val socketPath: String
) {
    protected var socket: Socket? = null
    protected val gson = Gson()
    protected val mainThreadHandler = Handler(Looper.getMainLooper())

    protected abstract val TAG: String


    private suspend fun initializeSocketInternal() {
        if (socket != null) {
            Log.d(TAG, "Socket object already exists for path $socketPath.")
            return
        }

        val token = authRepository.getToken()
        if (token == null) {
            Log.w(
                TAG,
                "Authentication token is null. Socket for $socketPath cannot be initialized at this moment."
            )
            return
        }

        try {
            val opts = IO.Options().apply {
                auth = mapOf("token" to token)
                reconnection = true
            }
            val fullSocketUrl = "$SOCKET_BASE_URL$socketPath"
            socket = IO.socket(fullSocketUrl, opts)
            Log.i(TAG, "Socket object created for URL: $fullSocketUrl with token.")
            setupDefaultSocketEventHandlers()
        } catch (e: URISyntaxException) {
            Log.e(TAG, "URISyntaxException during socket initialization for $socketPath", e)
            socket = null
        } catch (e: Exception) {
            Log.e(TAG, "General exception during socket initialization for $socketPath", e)
            socket = null
        }
    }

    open suspend fun connect() {
        if (socket == null) {
            Log.d(TAG, "Socket for $socketPath not yet initialized. Attempting to initialize now.")
            initializeSocketInternal()
        }

        if (socket == null) {
            Log.w(TAG, "Socket initialization failed for $socketPath. Cannot connect.")
            return
        }

        if (socket?.connected() == false) {
            Log.i(TAG, "Socket for $socketPath not connected. Attempting to connect...")
            socket?.connect()
        } else if (socket?.connected() == true) {
            Log.d(TAG, "Socket for $socketPath is already connected.")
        }
    }

    open fun disconnect() {
        if (socket?.connected() == true) {
            Log.i(TAG, "Disconnecting socket for $socketPath...")
            socket?.disconnect()
        } else {
            Log.d(TAG, "Socket for $socketPath already disconnected or not initialized.")
        }
    }

    fun isConnected(): Boolean = socket?.connected() == true

    protected open fun setupDefaultSocketEventHandlers() {
        socket?.on(Socket.EVENT_CONNECT) {
            Log.i(TAG, "Socket connected successfully for $socketPath!")
        }
        socket?.on(Socket.EVENT_DISCONNECT) { args ->
            val reason = if (args.isNotEmpty() && args[0] != null) args[0].toString() else "N/A"
            Log.i(TAG, "Socket for $socketPath disconnected. Reason: $reason")
        }
        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            val errorDetails =
                if (args.isNotEmpty() && args[0] != null) args[0].toString() else "No details"
            Log.e(TAG, "Socket connection error for $socketPath: $errorDetails")
        }
    }

    protected fun emitEvent(eventName: String, data: JSONObject? = null) {
        if (socket == null) {
            Log.w(
                TAG,
                "Socket for $socketPath not initialized. Cannot emit event '$eventName'. Call connect() first."
            )
            return
        }
        if (socket?.connected() == true) {
            if (data != null) {
                socket?.emit(eventName, data)
                Log.d(TAG, "Emitted '$eventName' for $socketPath with data: $data")
            } else {
                socket?.emit(eventName)
                Log.d(TAG, "Emitted '$eventName' for $socketPath without data")
            }
        } else {
            Log.w(
                TAG,
                "Cannot emit event '$eventName' for $socketPath, socket not connected. Call connect() first."
            )
        }
    }

    protected fun <T> onEvent(
        eventName: String,
        responseClass: Class<T>,
        callback: (data: T) -> Unit
    ): Emitter.Listener {
        val listener = Emitter.Listener { args ->
            Log.d(
                TAG,
                "Received '$eventName' for $socketPath with data: ${args.joinToString { it?.toString() ?: "null" }}"
            )
            if (args.isNotEmpty() && args[0] != null) {
                val data = args[0]
                try {
                    val eventJsonString = when (data) {
                        is JSONObject -> data.toString()
                        is String -> data
                        else -> {
                            Log.w(
                                TAG,
                                "Unexpected data type for '$eventName' on $socketPath: ${data.javaClass.name}"
                            )
                            return@Listener
                        }
                    }
                    val parsedData = gson.fromJson(eventJsonString, responseClass)
                    mainThreadHandler.post { callback(parsedData) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing '$eventName' data on $socketPath", e)
                }
            } else {
                Log.w(TAG, "Received '$eventName' for $socketPath with no data or null data.")
            }
        }
        if (socket == null) {
            Log.w(
                TAG,
                "Socket for $socketPath not initialized. Cannot set listener for '$eventName'."
            )
            return Emitter.Listener {}
        }
        socket?.on(eventName, listener)
        Log.d(TAG, "Subscribed to '$eventName' for $socketPath")
        return listener
    }

    protected fun offEvent(eventName: String, listener: Emitter.Listener?) {
        if (listener == null) return
        socket?.off(eventName, listener)
        Log.d(TAG, "Unsubscribed specific listener from '$eventName' for $socketPath")
    }

    protected fun offAllForEvent(eventName: String) {
        socket?.off(eventName)
        Log.d(TAG, "Unsubscribed all listeners from '$eventName' for $socketPath")
    }

    protected fun clearSocket() {
        socket?.off()
        socket?.disconnect()
        socket = null
        Log.i(TAG, "Socket resources cleared for $socketPath.")
    }

    fun onConnect(callback: () -> Unit): Emitter.Listener {
        return onEvent("connect", Unit::class.java, callback = { callback() })
    }
}