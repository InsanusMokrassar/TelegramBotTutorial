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
 * This is template of plugin with preset [log]ger, [Config] and template configurations of [setupDI] and [setupBotPlugin].
 * Replace [pluginConfigSectionName] value with your one to customize configuration section name
 */
@Serializable
class {{.module_name}}Plugin : Plugin {
    /**
     * Default logger of [WelcomePlugin] got with [logger]
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
    )

    /**
     * DI configuration of current plugin. Here we are decoding [Config] and put it into [Module] receiver
     */
    override fun Module.setupDI(database: Database, params: JsonObject) {
        single { get<Json>().decodeFromJsonElement(Config.serializer(), params[pluginConfigSectionName] ?: return@single null) }
    }

    /**
     * Final configuration of bot. Here we are getting [Config] from [koin]
     */
    override suspend fun BehaviourContext.setupBotPlugin(koin: Koin) {
        val config = koin.getOrNull<Config>()

        if (config == null) {
            log.w("Plugin has been disabled due to absence of \"$pluginConfigSectionName\" field in config or some error during configuration loading")
            return
        }
    }

    companion object {
        private const val pluginConfigSectionName = "new"
    }
}
