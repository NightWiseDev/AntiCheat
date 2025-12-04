package net.impossibleworld.anticheat.check

import com.github.retrooper.packetevents.event.PacketEvent
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import net.impossibleworld.anticheat.Main
import net.impossibleworld.anticheat.data.PlayerData

class ClickConsistencyCheck(data : PlayerData) : Check(data,"ClickConsistencyCheck") {

    private val mainConfig = Main.instance.getWorkConfig()

    override fun handle(event: PacketReceiveEvent) {
        if (event.packetType == PacketType.Play.Client.ANIMATION) {
            val now = System.currentTimeMillis()

            // 1. Проверка на паузу (сброс сессии кликов)
            if (data.lastSwingTime == 0L || (now - data.lastSwingTime) > 500) {
                data.clearClickSamples()
                data.lastSwingTime = now
                return
            }

            val delay = now - data.lastSwingTime

            // 2. Добавляем сэмпл
            data.addClickSample(delay)

            // ВАЖНО: Обновляем время для следующего расчета!
            data.lastSwingTime = now

            // 3. Если набрали 20 ударов - проверяем
            if (data.getClickSamples().size == 20) {
                val samples = data.getClickSamples()
                val averageDelay = samples.average()
                val stdDev = getStdDev(samples) // Твоя функция

                // Только если кликает быстро (> 6.6 CPS)
                if (averageDelay < 150) {

                    // Проверяем стабильность
                    if (stdDev < 12.0) {
                        // --- ЧИТЕР (Слишком ровно) ---
                        if (increaseBuffer(1.0, 0.5, 3.0)) {
                            val failMessage = mainConfig.getMessage("fails.auto_clicker") // Убедись, что переменная доступна
                                .replace("%avg%", averageDelay.toInt().toString())
                                .replace("%dev%", String.format("%.2f", stdDev))

                            fail(failMessage)
                            event.isCancelled = true // Для кликов лучше не отменять, чтобы не лагало пвп
                        }
                    } else {
                        // --- ЧЕСТНЫЙ (Разброс большой) ---
                        // Уменьшаем буфер, если игрок кликает руками
                        decreaseBuffer(0.5)
                    }
                }

                // ВАЖНО: Очищаем список ТОЛЬКО после проверки
                data.clearClickSamples()
            }
        }
    }

    private fun getStdDev(delays : List<Long>) : Double {
        if(delays.isEmpty()) return 0.0

        // 1. Считаем среднее арифметическое (Average)
        // Пример: (100 + 102 + 98) / 3 = 100
        val average = delays.average()

        var variance = 0.0
        for (delay in delays) {
            variance += Math.pow(delay - average,2.0)
        }
        // извлечение корня
        return Math.sqrt(variance / delays.size)
    }
}