package com.example.geometric_neon_runner.game.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.LinearGradient
import android.graphics.Shader
import com.example.geometric_neon_runner.game.entities.Enemy
import com.example.geometric_neon_runner.game.entities.EnemyShape
import com.example.geometric_neon_runner.game.entities.Player
import kotlin.math.sin

class GameRenderer(
    var screenWidth: Int,
    var screenHeight: Int
) {

    private var gridOffsetY = 0f
    private var animationTime = 0f

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val lanePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 4f
        style = Paint.Style.STROKE
        setShadowLayer(12f, 0f, 0f, 0xFF00FFFF.toInt())
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun render(canvas: Canvas, player: Player, enemies: List<Enemy>) {
        drawSimpleNeonBackground(canvas)
        drawCleanGrid(canvas)
        drawLanes(canvas)
        drawEnemies(canvas, enemies)
        drawPlayer(canvas, player)
    }

    private fun drawSimpleNeonBackground(canvas: Canvas) {
        canvas.drawRGB(8, 8, 20)

        val gradient = LinearGradient(
            0f, 0f,
            0f, screenHeight.toFloat(),
            intArrayOf(
                0xFF0a0a1a.toInt(),
                0xFF1a0a2a.toInt()
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )

        backgroundPaint.shader = gradient
        backgroundPaint.alpha = 120
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), backgroundPaint)

        drawSoftStars(canvas)
    }

    private fun drawSoftStars(canvas: Canvas) {
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        for (i in 0 until 8) {
            val x = ((i * 97 + animationTime * 20) % screenWidth).toFloat()
            val y = ((i * 157 + animationTime * 15) % screenHeight).toFloat()

            val twinkle = (0.4f + 0.3f * sin(animationTime * 2f + i * 0.5f)).toFloat()
            val size = 1.5f * twinkle

            starPaint.color = if (i % 2 == 0) 0x6600FFFF.toInt() else 0x66FFFFFF.toInt()

            canvas.drawCircle(x, y, size, starPaint)
        }
    }

    private fun drawCleanGrid(canvas: Canvas) {
        val spacing = 120f
        var offset = gridOffsetY % spacing
        if (offset < 0) offset += spacing

        var y = -spacing + offset

        while (y < screenHeight + spacing) {
            gridPaint.color = 0x2200AAFF.toInt()
            gridPaint.strokeWidth = 1.5f

            canvas.drawLine(0f, y, screenWidth.toFloat(), y, gridPaint)
            y += spacing
        }
    }

    fun drawLanes(canvas: Canvas) {
        val leftSep = screenWidth * 0.375f
        val rightSep = screenWidth * 0.625f

        val pulse = (0.6f + 0.2f * sin(animationTime * 2.5).toFloat())

        lanePaint.color = 0xFF00DDFF.toInt()
        lanePaint.alpha = (180 * pulse).toInt()

        canvas.drawLine(leftSep, 0f, leftSep, screenHeight.toFloat(), lanePaint)
        canvas.drawLine(rightSep, 0f, rightSep, screenHeight.toFloat(), lanePaint)
    }

    fun drawPlayer(canvas: Canvas, player: Player) {
        player.draw(canvas)
    }

    fun drawEnemies(canvas: Canvas, enemies: List<Enemy>) {
        for (e in enemies) {
            val baseColor = when (e.shape) {
                EnemyShape.TRIANGLE -> 0xFF00FFFF.toInt()
                EnemyShape.SQUARE -> 0xFFFF00FF.toInt()
                EnemyShape.CIRCLE -> 0xFF00FF88.toInt()
                EnemyShape.HEXAGON -> 0xFFFFAA00.toInt()
                EnemyShape.DIAMOND -> 0xFFFF0088.toInt()
            }

            if (e.isInDangerZone()) {
                e.setColor(0xFFFF4444.toInt())
            } else {
                e.setColor(baseColor)
            }

            e.draw(canvas)
        }
    }

    fun update(deltaTime: Float) {
        val scrollSpeed = 120f
        gridOffsetY += scrollSpeed * deltaTime
        animationTime += deltaTime
    }
}