import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.contact
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton

fun main() {
    val bot = bot {
        token = ""
        val applicationData = mutableMapOf<Long, Application>()
        val stages = mutableMapOf<Long, Int>()

        dispatch {
            command("start") {
                stages[message.chat.id] = 0
            }

            message {
                val chatId = message.chat.id
                val userMessage = message.text.orEmpty()

                when (stages[chatId]) {
                    0 -> {
                        val keyboardMarkup = KeyboardReplyMarkup(
                            keyboard = listOf(
                                listOf(KeyboardButton("Онлайн-консультация")),
                                listOf(KeyboardButton("Очная консультация в г. Симферополь")),
                            ),
                            resizeKeyboard = true,
                            oneTimeKeyboard = true,
                        )

                        bot.sendMessage(
                            chatId = ChatId.fromId(message.chat.id),
                            text = "Привет! Выберите тип консультации:",
                            replyMarkup = keyboardMarkup
                        )
                        stages[chatId] = 1
                    }

                    1 -> {
                        applicationData[chatId] = Application(type = userMessage)

                        val keyboardMarkup = KeyboardReplyMarkup(
                            keyboard = listOf(
                                listOf(KeyboardButton("Эта неделя")),
                                listOf(KeyboardButton("Следующая неделя")),
                            ),
                            resizeKeyboard = true,
                            oneTimeKeyboard = true
                        )

                        bot.sendMessage(
                            chatId = ChatId.fromId(chatId),
                            text = "Выберите неделю:",
                            replyMarkup = keyboardMarkup,
                        )
                        stages[chatId] = 2
                    }

                    2 -> {
                        applicationData[chatId]?.week = userMessage

                        val keyboardMarkup = KeyboardReplyMarkup(
                            keyboard = listOf(
                                listOf(
                                    KeyboardButton(text = "Понедельник"),
                                    KeyboardButton(text = "Вторник"),
                                ),
                                listOf(
                                    KeyboardButton(text = "Среда"),
                                    KeyboardButton(text = "Четверг"),
                                ),
                                listOf(
                                    KeyboardButton(text = "Пятница"),
                                    KeyboardButton(text = "Суббота"),
                                ),
                                listOf(
                                    KeyboardButton(text = "Воскресенье"),
                                ),
                            ),
                            resizeKeyboard = true,
                            oneTimeKeyboard = true
                        )

                        bot.sendMessage(
                            chatId = ChatId.fromId(chatId),
                            text = "Выберите предпочитаемый день недели:",
                            replyMarkup = keyboardMarkup,
                        )
                        stages[chatId] = 3
                    }

                    3 -> {
                        applicationData[chatId]?.day = userMessage

                        val keyboardMarkup = KeyboardReplyMarkup(
                            keyboard = listOf(
                                listOf(KeyboardButton(text = "8:00 - 12:00")),
                                listOf(KeyboardButton(text = "12:30 - 16:00")),
                                listOf(KeyboardButton(text = "16:30 - 19:30")),
                            ),
                            resizeKeyboard = true,
                            oneTimeKeyboard = true
                        )

                        bot.sendMessage(
                            chatId = ChatId.fromId(chatId),
                            text = "Выберите предпочитаемое время:",
                            replyMarkup = keyboardMarkup,
                        )
                        stages[chatId] = 4
                    }

                    4 -> {
                        applicationData[chatId]?.time = userMessage
                        bot.sendMessage(chatId = ChatId.fromId(chatId), text = "Краткое описание проблемы:")
                        stages[chatId] = 5
                    }

                    5 -> {
                        applicationData[chatId]?.description = userMessage
                        val keyboardMarkup = KeyboardReplyMarkup(
                            keyboard = listOf(
                                listOf(
                                    KeyboardButton(
                                        text = "Отправить мой номер телефона",
                                        requestContact = true
                                    )
                                )
                            ),
                            resizeKeyboard = true,
                            oneTimeKeyboard = true
                        )
                        bot.sendMessage(
                            chatId = ChatId.fromId(chatId),
                            text = "Пожалуйста, отправьте ваш контактный номер телефона, используя кнопку ниже.",
                            replyMarkup = keyboardMarkup
                        )
                        stages.remove(chatId)
                    }
                }
            }

            contact {
                val chatId = message.chat.id
                val contact = message.contact
                val phoneNumber = contact?.phoneNumber

                applicationData[chatId]?.username = message.from?.username
                applicationData[chatId]?.phoneNumber = phoneNumber
                applicationData[chatId]?.name = contact?.firstName + " " + contact?.lastName
                bot.sendMessage(chatId = ChatId.fromId(chatId), text = "Спасибо, заявка принята, ожидайте ответа")

                val application = applicationData[chatId]
                if (application != null) {
                    val drChatId = ChatId.fromId(888536421)
                    val myChatId = ChatId.fromId(307805298)

                    bot.sendMessage(chatId = drChatId, text = application.toString())
//                    bot.sendMessage(chatId = myChatId, text = application.toString())
                }
                applicationData.remove(chatId)

                val keyboardMarkup = KeyboardReplyMarkup(
                    keyboard = listOf(
                        listOf(
                            KeyboardButton(text = "/start")
                        )
                    ),
                    resizeKeyboard = true,
                    oneTimeKeyboard = true
                )
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Нажмите '/start' для подачи новой заявки.",
                    replyMarkup = keyboardMarkup
                )
            }
        }
    }

    bot.startPolling()
}


data class Application(
    var type: String? = null,
    var week: String? = null,
    var day: String? = null,
    var time: String? = null,
    var description: String? = null,
    var username: String? = null,
    var phoneNumber: String? = null,
    var name: String? = null,
) {
    override fun toString(): String =
        "Поступила новая заявка на констультацию:\n\n" +
                "Имя и фамилия из профиля ТГ: $name\n" +
                "Тип консультации: $type\n" +
                "Какая неделя: $week\n" +
                "День недели: $day\n" +
                "Время: $time\n" +
                "Описание: $description\n" +
                "Логин пользователя: @$username\n" +
                "Номер телефона: $phoneNumber"
}