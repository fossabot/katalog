package com.bol.katalog.config

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer
import org.springframework.boot.diagnostics.FailureAnalysis

class KatalogFailureAnalyzer : AbstractFailureAnalyzer<KatalogStartupException>() {
    override fun analyze(rootFailure: Throwable?, cause: KatalogStartupException?): FailureAnalysis {
        val (description, action) = getAnalysis(cause)
        return FailureAnalysis(description, action, cause)
    }

    private fun getAnalysis(failure: Throwable?): Pair<String, String> = when (failure) {
        is KatalogStartupException -> Pair(failure.message ?: "No description provided", failure.action)
        else -> Pair("Unknown description", "Unknown action")
    }
}