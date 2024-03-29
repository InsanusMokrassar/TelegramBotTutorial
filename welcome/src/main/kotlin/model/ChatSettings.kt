package model

import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.MessageIdentifier
import kotlinx.serialization.Serializable

@Serializable
internal data class ChatSettings(
    val targetChatId: IdChatIdentifier,
    val sourceChatId: IdChatIdentifier,
    val sourceMessageId: MessageIdentifier
)
