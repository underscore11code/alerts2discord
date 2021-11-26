package io.u11.alerts2discord

import io.javalin.Javalin
import io.javalin.http.HttpCode
import io.u11.alerts2discord.util.Config
import io.u11.alerts2discord.util.logger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.TextChannel
import java.io.File
import kotlin.system.exitProcess

fun main() {
    logger("bootstrap").info("Hello World! Starting Alerts2Discord...")
    Alerts2Discord
}

object Alerts2Discord {
    private val logger = logger("Alerts2Discord")

    val config = Config(A2DConfig::class.java, File("config.conf")).also {
        logger.info("Loading config ${it.file.absolutePath}")
        it.load()
        it.save()
        logger.info("Loaded config:")
        logger.info(it().toString())
        if (it().configs["default"] == null) {
            logger.error("\"default\" config not found!")
            exitProcess(1)
        }
    }

    val javalin = Javalin.create {
        it.showJavalinBanner = false
        it.enableCorsForAllOrigins()
    }.start(9876).also {
        logger.info("Setting up endpoint...")
        it.post("/") { ctx ->
            ctx.status(HttpCode.OK)
            handle(
                config().configs[ctx.queryParam("config")] ?: config().defaultConfig,
                json.decodeFromString<PromAlertGroupModel>(ctx.body())
            )
        }
        logger.info("Done.")
    }

    val jda = JDABuilder
        .createDefault(config().token())
        .setActivity(Activity.watching("for alerts"))
        .build()

    val json = Json {
        ignoreUnknownKeys = true

    }

    private fun handle(config: AlertConfig, alertGroup: PromAlertGroupModel) {
        for (alert in alertGroup.alerts) {
            val channel = jda.getTextChannelById(config.discordChannelId) ?:
              return logger.error("Could not find channel for id ${config.discordChannelId}")
            channel.sendMessage(
                setDiscordPlaceholders(
                    setAlertPlaceholders(
                        if (alert.status == "firing") config.messageFire else config.messageResolve,
                        ParentedAlert(alert, alertGroup)
                    ),
                    channel)
            ).queue()
        }
    }

    private data class ParentedAlert(val alert: PromAlertModel, val group: PromAlertGroupModel)

    private fun setAlertPlaceholders(text: String, alert: ParentedAlert) : String {
        var tmp = text.replace("%alert_status%", alert.alert.status)
        alert.alert.labels.forEach {
            tmp = tmp.replace("%alert_label_${it.key.lowercase()}%", it.value)
        }

        alert.alert.annotations.forEach {
            tmp = tmp.replace("%alert_annotation_${it.key.lowercase()}%", it.value)
        }

        tmp = tmp.replace("%group_status%", alert.group.status)
        alert.group.commonLabels.forEach {
            tmp = tmp.replace("%group_label_${it.key.lowercase()}%", it.value)
        }

        alert.group.commonAnnotations.forEach {
            tmp = tmp.replace("%group_annotation_${it.key.lowercase()}%", it.value)
        }

        return tmp
    }

    private fun setDiscordPlaceholders(text: String, channel: TextChannel) : String {
        var tmp = text.replace("@owner", "<@${channel.guild.ownerId}>", true)

        channel.guild.roles.forEach {
            tmp = tmp.replace("@&${it.name}", it.asMention, true)
        }

        channel.guild.channels.forEach {
            tmp = tmp.replace("#${it.name}", it.asMention, true)
        }

        return tmp
    }
}

//{
//  "version": "4",
//  "groupKey": <string>,              // key identifying the group of alerts (e.g. to deduplicate)
//  "truncatedAlerts": <int>,          // how many alerts have been truncated due to "max_alerts"
//  "status": "<resolved|firing>",
//  "receiver": <string>,
//  "groupLabels": <object>,
//  "commonLabels": <object>,
//  "commonAnnotations": <object>,
//  "externalURL": <string>,           // backlink to the Alertmanager.
//  "alerts": [
//    {
//      "status": "<resolved|firing>",
//      "labels": <object>,
//      "annotations": <object>,
//      "startsAt": "<rfc3339>",
//      "endsAt": "<rfc3339>",
//      "generatorURL": <string>,      // identifies the entity that caused the alert
//      "fingerprint": <string>        // fingerprint to identify the alert
//    },
//    ...
//  ]
//}


//{
//  "receiver": "test",
//  "status": "firing",
//  "alerts": [
//    {
//      "status": "firing",
//      "labels": {
//        "alertname": "BlackboxSlowProbe",
//        "instance": "http://play.wolvhaven.net:8804",
//        "job": "blackbox",
//        "severity": "warning"
//      },
//      "annotations": {
//        "description": "Blackbox probe took more than 2s to complete\n  VALUE = 2.487369155333333\n  LABELS = map[instance:http://play.wolvhaven.net:8804 job:blackbox]",
//        "summary": "Blackbox slow probe (instance http://play.wolvhaven.net:8804)"
//      },
//      "startsAt": "2021-11-26T02:04:33.716Z",
//      "endsAt": "0001-01-01T00:00:00Z",
//      "generatorURL": "http://252a9bd57a18:9090/graph?g0.expr=avg_over_time%28probe_duration_seconds%5B1m%5D%29+%3E+2&g0.tab=1",
//      "fingerprint": "37913954405f8a39"
//    },
//    {
//      "status": "resolved",
//      "labels": {
//        "alertname": "BlackboxSlowProbe",
//        "instance": "https://wolvhaven.net",
//        "job": "blackbox",
//        "severity": "warning"
//      },
//      "annotations": {
//        "description": "Blackbox probe took more than 2s to complete\n  VALUE = 2.68206315225\n  LABELS = map[instance:https://wolvhaven.net job:blackbox]",
//        "summary": "Blackbox slow probe (instance https://wolvhaven.net)"
//      },
//      "startsAt": "2021-11-26T02:44:33.716Z",
//      "endsAt": "2021-11-26T02:45:33.716Z",
//      "generatorURL": "http://252a9bd57a18:9090/graph?g0.expr=avg_over_time%28probe_duration_seconds%5B1m%5D%29+%3E+2&g0.tab=1",
//      "fingerprint": "6d0038331057f26c"
//    }
//  ],
//  "groupLabels": {
//    "alertname": "BlackboxSlowProbe"
//  },
//  "commonLabels": {
//    "alertname": "BlackboxSlowProbe",
//    "job": "blackbox",
//    "severity": "warning"
//  },
//  "commonAnnotations": {},
//  "externalURL": "http://2847935c28e0:9093",
//  "version": "4",
//  "groupKey": "{}:{alertname=\"BlackboxSlowProbe\"}",
//  "truncatedAlerts": 0
//}
