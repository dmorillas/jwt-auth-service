package co.morillas.auth.context

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

object MetricsProvider {
    val appMicrometerRegistry by lazy {
        PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    }
}