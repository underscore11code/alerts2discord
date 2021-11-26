package io.u11.alerts2discord

import io.u11.alerts2discord.util.logger
import kotlinx.serialization.Serializable
import kotlin.system.exitProcess

@Serializable
data class PromAlertGroupModel(
    val version: String, // 4
    val status: String, // resolved / firing
    val alerts: List<PromAlertModel>,
    val commonLabels: Map<String, String>, // ?
    val groupLabels: Map<String, String>, // ?
    val commonAnnotations: Map<String, String>, // ?
) {
    init {
        if (version != "4") {
            logger().error("Unexpected version! Expected 4, got {}", version)
            exitProcess(1)
        }
    }
}

@Serializable
data class PromAlertModel(
    val status: String,
    val labels: Map<String, String>, // ?
    val annotations: Map<String, String>, // ?
)

val PromAlertModel.alertName get() = labels["alertname"]
val PromAlertModel.instance get() = labels["instance"]
val PromAlertModel.job get() = labels["job"]
val PromAlertModel.severity get() = labels["severity"]
val PromAlertModel.description get() = annotations["description"]

val PromAlertGroupModel.alertName get() = commonLabels["alertname"]
val PromAlertGroupModel.job get() = commonLabels["job"]
val PromAlertGroupModel.severity get() = commonLabels["severity"]
