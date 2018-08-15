package com.bol.blueprint.config

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer
import org.springframework.boot.diagnostics.FailureAnalysis

class BlueprintFailureAnalyzer : AbstractFailureAnalyzer<BlueprintStartupException>() {
    override fun analyze(rootFailure: Throwable?, cause: BlueprintStartupException?): FailureAnalysis {
        val (description, action) = getAnalysis(cause)
        return FailureAnalysis(description, action, cause)
    }

    private fun getAnalysis(failure: Throwable?): Pair<String, String> = when (failure) {
        is BlueprintStartupException -> Pair(failure.message ?: "No description provided", failure.action)
        else -> Pair("Unknown description", "Unknown action")
    }
}