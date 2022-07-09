import db.WelcomeTable
import dev.inmo.kslog.common.logger
import dev.inmo.kslog.common.w
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.coroutines.subscribeSafelyWithoutExceptions
import dev.inmo.plagubot.Plugin
import dev.inmo.plagubot.plugins.commands.full
import dev.inmo.tgbotapi.bot.exceptions.RequestException
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.chat.get.getChatAdministrators
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.api.send.*
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitContentMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.oneOf
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.*
import dev.inmo.tgbotapi.extensions.utils.*
import dev.inmo.tgbotapi.extensions.utils.formatting.*
import dev.inmo.tgbotapi.extensions.utils.types.buttons.*
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.MilliSeconds
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.message.abstracts.CommonGroupContentMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import model.ChatSettings
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.core.qualifier.named

/**
 * This is template of plugin with preset [log]ger, [Config] and template configurations of [setupDI] and [setupBotPlugin].
 * Replace [pluginConfigSectionName] value with your one to customize configuration section name
 */
@Serializable
class WelcomePlugin : Plugin {
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
     *
     * @param recheckOfAdmin This parameter will be used before setup of
     */
    @Serializable
    private class Config(
        val recheckOfAdmin: MilliSeconds = 60L
    )

    /**
     * DI configuration of current plugin. Here we are decoding [Config] and put it into [Module] receiver
     */
    override fun Module.setupDI(database: Database, params: JsonObject) {
        single { get<Json>().decodeFromJsonElement(Config.serializer(), params[pluginConfigSectionName] ?: return@single Config()) }
        single { WelcomeTable(database) }
        single(named("welcome")) { BotCommand("welcome", "Use to setup welcome message").full(BotCommandScope.AllChatAdministrators) }
    }

    private suspend fun BehaviourContext.handleWelcomeCommand(
        welcomeTable: WelcomeTable,
        config: Config,
        groupMessage: CommonGroupContentMessage<MessageContent>
    ) {
        val user = groupMessage.user

        if (userIsAdmin(user, groupMessage.chat)) {
            val cancelData = "cancel_${groupMessage.chat.id}"
            val unsetData = "unset_${groupMessage.chat.id}"

            val sentMessage = sendMessage(
                user,
                buildEntities {
                    regular("Ok, send me the message which should be used as welcome message for chat ")
                    underline(groupMessage.chat.title)
                },
                replyMarkup = inlineKeyboard {
                    row {
                        dataButton("Unset", unsetData)
                        dataButton("Cancel", cancelData)
                    }
                }
            )

            oneOf(
                async {
                    val query = waitMessageDataCallbackQuery().filter {
                        it.data == unsetData
                            && it.message.chat.id == sentMessage.chat.id
                            && it.message.messageId == sentMessage.messageId
                    }.first()

                    if (welcomeTable.unset(groupMessage.chat.id)) {
                        edit(
                            sentMessage,
                            buildEntities {
                                regular("Welcome message has been removed for chat ")
                                underline(groupMessage.chat.title)
                            }
                        )
                    } else {
                        edit(
                            sentMessage,
                            buildEntities {
                                regular("Something went wrong on welcome message unsetting for chat ")
                                underline(groupMessage.chat.title)
                            }
                        )
                    }

                    answer(query)
                },
                async {
                    val query = waitMessageDataCallbackQuery().filter {
                        it.data == cancelData
                            && it.message.chat.id == sentMessage.chat.id
                            && it.message.messageId == sentMessage.messageId
                    }.first()

                    edit(
                        sentMessage,
                        buildEntities {
                            regular("You have cancelled change of welcome message for chat ")
                            underline(groupMessage.chat.title)
                        }
                    )

                    answer(query)
                },
                async {
                    val message = waitContentMessage().filter {
                        it.chat.id == sentMessage.chat.id
                    }.first()

                    val success = welcomeTable.set(
                        ChatSettings(
                            groupMessage.chat.id,
                            message.chat.id,
                            message.messageId
                        )
                    )

                    if (success) {
                        reply(
                            message,
                            buildEntities {
                                regular("Welcome message has been changed for chat ")
                                underline(groupMessage.chat.title)
                                regular(".\n\n")
                                bold("Please, do not delete this message if you want it to work and don't stop this bot to keep welcome message works right")
                            }
                        )
                    } else {
                        reply(
                            message,
                            buildEntities {
                                regular("Something went wrong on welcome message changing for chat ")
                                underline(groupMessage.chat.title)
                            }
                        )
                    }
                },
                async {
                    while (isActive) {
                        delay(config.recheckOfAdmin)

                        if (!userIsAdmin(user, groupMessage.chat)) {
                            edit(sentMessage, "Sorry, but you are not admin in chat ${groupMessage.chat.title} more")
                            break
                        }
                    }
                }
            )
        }
    }

    /**
     * Final configuration of bot. Here we are getting [Config] from [koin]
     */
    override suspend fun BehaviourContext.setupBotPlugin(koin: Koin) {
        val config = koin.get<Config>()

        val welcomeTable = koin.get<WelcomeTable>()

        onCommand(
            "welcome",
            initialFilter = {
                it.chat is GroupChat
            }
        ) {
            it.whenCommonGroupContentMessage { groupMessage ->
                launch {
                    handleWelcomeCommand(welcomeTable, config, groupMessage)
                }
            }
        }

        onNewChatMembers {
            val chatSettings = welcomeTable.get(it.chat.id)

            if (chatSettings == null) {
                return@onNewChatMembers
            }

            try {
                copyMessage(
                    it.chat.id,
                    chatSettings.sourceChatId,
                    chatSettings.sourceMessageId
                )
            } catch (e: RequestException) {
                welcomeTable.unset(it.chat.id)
            }
        }


        allUpdatesFlow.subscribeSafelyWithoutExceptions(scope) {
            println(it)
        }
    }

    companion object {
        private const val pluginConfigSectionName = "welcome"
    }
}
