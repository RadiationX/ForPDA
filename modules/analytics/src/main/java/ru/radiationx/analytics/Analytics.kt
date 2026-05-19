package ru.radiationx.analytics

import android.app.Application
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

object Analytics {

    fun initAppMetrica(application: Application, apiKey: String) {
        val config = AppMetricaConfig.newConfigBuilder(apiKey).build()
        AppMetrica.activate(application, config)
        AppMetrica.enableActivityAutoTracking(application)
    }

    fun reportError(message: String, error: Throwable?) {

    }
}