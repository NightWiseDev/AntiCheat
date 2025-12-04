package net.impossibleworld.anticheat.utility

import net.md_5.bungee.api.ChatColor
import java.awt.Color
import java.util.regex.Matcher
import java.util.regex.Pattern

object HexUtil {

    // Паттерн для поиска HEX цветов в формате &#RRGGBB
    private val HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})")

    // Паттерн для градиентов (как в предыдущей версии)
    private val GRADIENT_PATTERN = Pattern.compile(
        "gradient:([#a-fA-F0-9]{6}):([#a-fA-F0-9]{6})\\s+(.*?)\\s*</gradient>",
        Pattern.CASE_INSENSITIVE
    )

    /**
     * Основной метод для перевода всех цветов (градиенты, HEX, legacy).
     */
    fun translate(message: String?): String {
        if (message.isNullOrEmpty()) return ""

        var result = message

        // 1. Обрабатываем градиенты
        result = processGradients(result)

        // 2. Обрабатываем HEX цвета (&#RRGGBB)
        val matcher = HEX_PATTERN.matcher(result)
        val buffer = StringBuffer()

        while (matcher.find()) {
            val hexCode = matcher.group(1)
            try {
                // ChatColor.of("#RRGGBB") возвращает объект цвета, toString() дает код формата §x§r...
                val color = ChatColor.of("#$hexCode")
                matcher.appendReplacement(buffer, color.toString())
            } catch (e: Exception) {
                // Если цвет некорректный, оставляем как есть
                matcher.appendReplacement(buffer, "&#$hexCode")
            }
        }
        matcher.appendTail(buffer)
        result = buffer.toString()

        // 3. Обрабатываем стандартные цвета (&a, &l и т.д.)
        return ChatColor.translateAlternateColorCodes('&', result)
    }

    /**
     * Переводит список строк.
     */
    fun translateList(lines: List<String>?): List<String> {
        return lines?.map { translate(it) } ?: emptyList()
    }

    private fun processGradients(input: String): String {
        val matcher = GRADIENT_PATTERN.matcher(input)
        val buffer = StringBuffer()

        while (matcher.find()) {
            val startHex = matcher.group(1)
            val endHex = matcher.group(2)
            val content = matcher.group(3)

            val startColor = hexToColor(startHex)
            val endColor = hexToColor(endHex)

            if (startColor != null && endColor != null) {
                val gradientText = createGradientText(content, startColor, endColor)
                // quoteReplacement нужен, чтобы спецсимволы в тексте (например $) не ломали замену
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(gradientText))
            } else {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(content))
            }
        }

        matcher.appendTail(buffer)
        return buffer.toString()
    }

    private fun createGradientText(text: String, start: Color, end: Color): String {
        val sb = StringBuilder()
        val length = text.length

        for (i in text.indices) {
            val ratio = if (length > 1) i.toFloat() / (length - 1) else 0f

            val r = (start.red * (1 - ratio) + end.red * ratio).toInt()
            val g = (start.green * (1 - ratio) + end.green * ratio).toInt()
            val b = (start.blue * (1 - ratio) + end.blue * ratio).toInt()

            // Формируем HEX строку для ChatColor
            val hexColor = String.format("#%02x%02x%02x", r, g, b)
            val chatColor = ChatColor.of(hexColor)

            sb.append(chatColor).append(text[i])
        }

        return sb.toString()
    }

    private fun hexToColor(hex: String): Color? {
        return try {
            val cleanHex = if (hex.startsWith("#")) hex else "#$hex"
            Color.decode(cleanHex)
        } catch (e: NumberFormatException) {
            null
        }
    }
}