import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.chat.get.getChatAdministrators
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.chat.User

suspend fun TelegramBot.userIsAdmin(user: User, chat: GroupChat): Boolean {
    val chatAdmins = getChatAdministrators(chat)
    val chatAdminsIds = chatAdmins.map { adminMember -> adminMember.user.id }

    return user.id in chatAdminsIds
}
