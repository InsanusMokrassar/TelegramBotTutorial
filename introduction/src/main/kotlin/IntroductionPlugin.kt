import dev.inmo.kslog.common.logger
import dev.inmo.kslog.common.w
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMyChatMemberUpdated
import dev.inmo.tgbotapi.types.chat.PrivateChat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module

@Serializable
class IntroductionPlugin : Plugin {
    private val log = logger

    @Serializable
    private class Config(
        val onStartCommandMessage: String
    )

    override fun Module.setupDI(database: Database, params: JsonObject) {
        single { get<Json>().decodeFromJsonElement(Config.serializer(), params["introduction"] ?: return@single null) }
    }

    override suspend fun BehaviourContext.setupBotPlugin(koin: Koin) {
        val config = koin.getOrNull<Config>()

        if (config == null) {
            log.w("Plugin has been disabled due to absence of \"introduction\" field in config or some error during configuration loading")
            return
        }

        onCommand("start", initialFilter = { it.chat is PrivateChat }) {
            sendMessage(
                it.chat,
                config.onStartCommandMessage
            )
        }
    }
}
