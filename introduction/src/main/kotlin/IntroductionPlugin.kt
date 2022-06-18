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

/**
 * This plugin represents simple plugin which realize all necessary functionality of plugin you may need in context of simple plugins.
 * In context of this plugin we will decode plugin configuration and setup reaction on `/start` command. In `introduction`
 * section of config you should put field `onStartCommandMessage` which will be sent to use on `/start` command
 */
@Serializable
class IntroductionPlugin : Plugin {
    /**
     * Default logger of [IntroductionPlugin] got with [logger]
     */
    private val log = logger

    /**
     * Configuration class for current plugin
     *
     * See realization of [setupDI] to get know how this class will be deserialized from global config
     *
     * See realization of [setupBotPlugin] to get know how to get access to this class
     */
    @Serializable
    private class Config(
        val onStartCommandMessage: String
    )

    /**
     * DI configuration of current plugin. Here we are decoding [Config] and put it into [Module] receiver
     */
    override fun Module.setupDI(database: Database, params: JsonObject) {
        single { get<Json>().decodeFromJsonElement(Config.serializer(), params["introduction"] ?: return@single null) }
    }

    /**
     * Final configuration of bot. Here we are getting [Config] from [koin] and configure reaction on `/start` command
     */
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
