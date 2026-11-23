package com.example.lifeclock

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min

class LifeClockWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = LifeClockEngine()

    private inner class LifeClockEngine : Engine() {
        private val prefs by lazy { getSharedPreferences("lifeclock", MODE_PRIVATE) }
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var visible = false
        private val handler = android.os.Handler(mainLooper)
        private val drawRunnable = object : Runnable {
            override fun run() {
                drawFrame()
                if (visible) handler.postDelayed(this, 60_000L)
            }
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            visible = isVisible
            handler.removeCallbacks(drawRunnable)
            if (visible) handler.post(drawRunnable)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            drawFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            visible = false
            handler.removeCallbacks(drawRunnable)
            super.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            visible = false
            handler.removeCallbacks(drawRunnable)
            super.onDestroy()
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) render(canvas)
            } catch (_: Exception) {
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
            }
        }

        private fun render(canvas: Canvas) {
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            canvas.drawColor(Color.rgb(8, 8, 8))

            val dobText = prefs.getString("dob", null)
            val dob = runCatching { dobText?.let(LocalDate::parse) }.getOrNull()
            if (dob == null || dob.isAfter(LocalDate.now())) {
                drawText(canvas, "Open LifeClock", w / 2, h * 0.48f, min(w, h) * 0.055f, Color.WHITE, true)
                drawText(canvas, "Set your date of birth", w / 2, h * 0.53f, min(w, h) * 0.027f, Color.LTGRAY, false)
                return
            }

            val stats = LifeMath.calculate(dob)
            val center = w / 2f
            drawText(canvas, "YOUR LIFE COUNTDOWN", center, h * 0.34f, w * 0.035f, Color.LTGRAY, false)
            drawText(canvas, "%,d".format(stats.remainingDays), center, h * 0.43f, w * 0.145f, Color.WHITE, true)
            drawText(canvas, "DAYS REMAINING", center, h * 0.48f, w * 0.032f, Color.LTGRAY, false)

            val barWidth = w * 0.68f
            val barHeight = maxOf(5f, w * 0.012f)
            val left = (w - barWidth) / 2f
            val top = h * 0.535f
            paint.color = Color.rgb(55, 55, 55)
            canvas.drawRoundRect(left, top, left + barWidth, top + barHeight, barHeight, barHeight, paint)
            paint.color = Color.WHITE
            canvas.drawRoundRect(left, top, left + barWidth * (stats.percentage / 100f), top + barHeight, barHeight, barHeight, paint)

            drawText(canvas, "${stats.percentage}% LIFE COMPLETED", center, h * 0.58f, w * 0.027f, Color.LTGRAY, false)
            drawText(canvas, "%,d DAYS LIVED".format(stats.livedDays), center, h * 0.64f, w * 0.031f, Color.WHITE, false)
            drawText(canvas, "%,d WEEKS REMAINING".format(stats.remainingWeeks), center, h * 0.68f, w * 0.031f, Color.WHITE, false)

            val quote = quotes[(LocalDate.now().toEpochDay() % quotes.size).toInt()]
            drawText(canvas, "“$quote”", center, h * 0.79f, w * 0.029f, Color.LTGRAY, false)
            drawText(canvas, "MAKE TODAY COUNT", center, h * 0.84f, w * 0.025f, Color.GRAY, false)
        }

        private fun drawText(
            canvas: Canvas, text: String, x: Float, y: Float,
            size: Float, color: Int, bold: Boolean
        ) {
            paint.color = color
            paint.textSize = size
            paint.typeface = if (bold) Typeface.create("sans-serif", Typeface.BOLD)
                             else Typeface.create("sans-serif", Typeface.NORMAL)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(text, x, y, paint)
        }
    }

    companion object {
        private val quotes = listOf(
            "Make the days count.",
            "The time is now.",
            "Small steps still move you forward.",
            "Spend your time on what matters.",
            "Begin before you feel ready.",
            "Be present. Be intentional.",
            "One day at a time."
        )
    }
}
