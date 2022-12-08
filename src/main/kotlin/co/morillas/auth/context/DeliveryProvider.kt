package co.morillas.auth.context

import co.morillas.auth.context.ActionsProvider.signIn
import co.morillas.auth.context.ActionsProvider.signUp
import co.morillas.auth.http.controller.auth.AuthController
import co.morillas.auth.http.controller.health.MetricsController
import co.morillas.auth.http.controller.health.StatusController

object DeliveryProvider {

    private val metricsController by lazy {
        MetricsController()
    }

    private val statusController by lazy {
        StatusController()
    }

    private val authController by lazy {
        AuthController(signUp, signIn)
    }

    val controllers by lazy {
        listOf(
            metricsController,
            statusController,
            authController
        )
    }
}