package db

import dev.inmo.micro_utils.repos.exposed.ExposedRepo
import dev.inmo.micro_utils.repos.exposed.initTable
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import model.ChatSettings
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

internal class WelcomeTable(
    override val database: Database
) : Table("welcome"), ExposedRepo {
    val targetChatIdColumn = long("targetChatId").uniqueIndex()
    val sourceChatIdColumn = long("sourceChatId")
    val sourceMessageIdColumn = long("sourceMessageId")
    override val primaryKey: PrimaryKey = PrimaryKey(targetChatIdColumn)

    init {
        initTable()
    }

    fun get(chatId: IdChatIdentifier): ChatSettings? = transaction(database) {
        select { targetChatIdColumn.eq(chatId.chatId) }.limit(1).firstOrNull() ?.let {
            ChatSettings(
                ChatId(it[targetChatIdColumn]),
                ChatId(it[sourceChatIdColumn]),
                it[sourceMessageIdColumn]
            )
        }
    }

    fun set(chatSettings: ChatSettings): Boolean = transaction(database) {
        deleteWhere { targetChatIdColumn.eq(chatSettings.targetChatId.chatId) }
        insert {
            it[targetChatIdColumn] = chatSettings.targetChatId.chatId
            it[sourceChatIdColumn] = chatSettings.sourceChatId.chatId
            it[sourceMessageIdColumn] = chatSettings.sourceMessageId
        }.insertedCount > 0
    }

    fun unset(chatId: IdChatIdentifier): Boolean = transaction(database) {
        deleteWhere { targetChatIdColumn.eq(chatId.chatId) } > 0
    }
}
