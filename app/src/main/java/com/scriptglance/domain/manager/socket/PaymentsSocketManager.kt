package com.scriptglance.domain.manager.socket

import com.scriptglance.data.model.payment.PaymentsEvent
import com.scriptglance.domain.repository.AuthRepository
import io.socket.emitter.Emitter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentsSocketManager @Inject constructor(
    authRepository: AuthRepository
) : BaseSocketManager(authRepository, SOCKET_PATH) {

    override val TAG: String = "PaymentsSocketManager"

    companion object {
        private const val SOCKET_PATH = "/payments"
        private const val EVENT_SUBSCRIBE_PAYMENTS = "subscribe_payments"
        private const val EVENT_PAYMENTS_EVENT = "payments_event"
    }

    fun subscribePayments() {
        emitEvent(EVENT_SUBSCRIBE_PAYMENTS)
    }

    fun onPaymentsEvent(callback: (event: PaymentsEvent) -> Unit): Emitter.Listener {
        return onEvent(EVENT_PAYMENTS_EVENT, PaymentsEvent::class.java, callback)
    }

    fun offPaymentsEvent(listener: Emitter.Listener?) {
        offEvent(EVENT_PAYMENTS_EVENT, listener)
    }
}