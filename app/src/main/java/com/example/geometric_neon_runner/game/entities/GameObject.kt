package com.example.geometric_neon_runner.game.entities

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

const val PLAYER_SIZE = 40f
const val ENEMY_SIZE = 40f


enum class EnemyShape {
    TRIANGLE, SQUARE, CIRCLE, HEXAGON, DIAMOND
}

class Player(
    var screenWidth: Int,
    var screenHeight: Int,
    initialLane: Int = 1
) {
    var currentLane: Int = initialLane
        private set

    var x: Float = 0f
    private var targetX: Float = 0f
    val y: Float = screenHeight * 0.85f
    val size: Float = PLAYER_SIZE

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        strokeWidth = 2f
        style = Paint.Style.FILL_AND_STROKE
        setShadowLayer(8f, 0f, 0f, 0x66FFFFFF.toInt())
    }
    private val path = Path()

    init {
        targetX = getLaneX(currentLane)
        x = targetX
    }

    fun moveToLane(lane: Int) {
        val clamped = lane.coerceIn(0, 2)
        currentLane = clamped
        targetX = getLaneX(clamped)
    }

    fun update(deltaTime: Float) {
        val lerpSpeed = 10f
        x += (targetX - x) * (1f - Math.exp((-lerpSpeed * deltaTime).toDouble())).toFloat()

        if (abs(targetX - x) < 0.5f) x = targetX
    }

    fun draw(canvas: Canvas) {
        path.reset()
        val half = size / 2f
        path.moveTo(x, y - half)
        path.lineTo(x - half, y + half)
        path.lineTo(x + half, y + half)
        path.close()
        canvas.drawPath(path, paint)
    }

    fun getLaneX(lane: Int): Float {
        val factor = when (lane.coerceIn(0,2)) {
            0 -> 0.25f
            1 -> 0.5f
            else -> 0.75f
        }
        return screenWidth * factor
    }
}

class Enemy(
    val screenWidth: Int,
    val screenHeight: Int,
    val lane: Int,
    startY: Float = -ENEMY_SIZE,
    var speed: Float = 400f,
    val shape: EnemyShape = EnemyShape.TRIANGLE
) {
    val x: Float = getLaneX(lane)
    var y: Float = startY
    val size: Float = ENEMY_SIZE

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f
        style = Paint.Style.FILL_AND_STROKE
        setShadowLayer(10f, 0f, 0f, 0x6600FFFF)
    }
    private val path = Path()

    fun setColor(colorInt: Int) {
        paint.color = colorInt
    }

    fun update(deltaTime: Float) {
        y += speed * deltaTime
    }

    fun draw(canvas: Canvas) {
        when (shape) {
            EnemyShape.TRIANGLE -> drawTriangle(canvas)
            EnemyShape.SQUARE -> drawSquare(canvas)
            EnemyShape.CIRCLE -> drawCircle(canvas)
            EnemyShape.HEXAGON -> drawHexagon(canvas)
            EnemyShape.DIAMOND -> drawDiamond(canvas)
        }
    }

    private fun drawTriangle(canvas: Canvas) {
        path.reset()
        val half = size / 2f
        path.moveTo(x, y + half)
        path.lineTo(x - half, y - half)
        path.lineTo(x + half, y - half)
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawSquare(canvas: Canvas) {
        val half = size / 2f
        canvas.drawRect(
            x - half,
            y - half,
            x + half,
            y + half,
            paint
        )
    }

    private fun drawCircle(canvas: Canvas) {
        canvas.drawCircle(x, y, size / 2f, paint)
    }

    private fun drawHexagon(canvas: Canvas) {
        path.reset()
        val radius = size / 2f
        for (i in 0..5) {
            val angle = (Math.PI / 3.0 * i).toFloat()
            val px = x + radius * cos(angle)
            val py = y + radius * sin(angle)
            if (i == 0) {
                path.moveTo(px, py)
            } else {
                path.lineTo(px, py)
            }
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawDiamond(canvas: Canvas) {
        path.reset()
        val half = size / 2f
        path.moveTo(x, y - half)
        path.lineTo(x + half, y)
        path.lineTo(x, y + half)
        path.lineTo(x - half, y)
        path.close()
        canvas.drawPath(path, paint)
    }

    fun isOffScreen(): Boolean {
        return y > screenHeight + 150f
    }

    fun isInDangerZone(): Boolean {
        return y > screenHeight * 0.8f
    }

    private fun getLaneX(lane: Int): Float {
        val factor = when (lane.coerceIn(0,2)) {
            0 -> 0.25f
            1 -> 0.5f
            else -> 0.75f
        }
        return screenWidth * factor
    }
}