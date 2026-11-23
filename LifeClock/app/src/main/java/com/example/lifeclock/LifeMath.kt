package com.example.lifeclock

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

data class LifeStats(
    val totalDays: Long,
    val livedDays: Long,
    val remainingDays: Long,
    val percentage: Int,
    val remainingWeeks: Long
)

object LifeMath {
    const val DEFAULT_LIFESPAN_YEARS = 60L

    fun calculate(dateOfBirth: LocalDate, today: LocalDate = LocalDate.now()): LifeStats {
        require(!dateOfBirth.isAfter(today)) { "Date of birth cannot be in the future." }

        val endDate = dateOfBirth.plusYears(DEFAULT_LIFESPAN_YEARS)
        val total = ChronoUnit.DAYS.between(dateOfBirth, endDate).coerceAtLeast(1)
        val lived = ChronoUnit.DAYS.between(dateOfBirth, today).coerceIn(0, total)
        val remaining = max(0L, total - lived)
        val percent = ((lived.toDouble() / total.toDouble()) * 100.0).toInt().coerceIn(0, 100)

        return LifeStats(
            totalDays = total,
            livedDays = lived,
            remainingDays = remaining,
            percentage = percent,
            remainingWeeks = (remaining + 6) / 7
        )
    }
}
