package com.example.geometric_neon_runner.game.systems

import com.example.geometric_neon_runner.game.entities.ENEMY_SIZE
import com.example.geometric_neon_runner.game.entities.Enemy
import com.example.geometric_neon_runner.game.entities.EnemyShape
import kotlin.math.hypot
import kotlin.random.Random

enum class SpawnMode { NORMAL, HARD, EXTREME }

class SpawnSystem(
    private val screenWidth: Int,
    private val screenHeight: Int,
    var mode: SpawnMode = SpawnMode.NORMAL
) {
    private val enemiesMutable = ArrayList<Enemy>(100)
    private var spawnTimer = 0f
    private var elapsedTime = 0f

    var spawnInterval: Float = when (mode) {
        SpawnMode.NORMAL -> 1.2f
        SpawnMode.HARD -> 0.85f
        SpawnMode.EXTREME -> 0.6f
    }

    private val fixedSpeed: Float = when (mode) {
        SpawnMode.NORMAL -> 1000f
        SpawnMode.HARD -> 1500f
        SpawnMode.EXTREME -> 1900f
    }

    fun update(deltaTime: Float) {
        elapsedTime += deltaTime
        spawnTimer += deltaTime

        if (spawnTimer >= spawnInterval) {
            spawnTimer -= spawnInterval
            spawnPattern()
        }
    }

    private fun spawnEnemy(lane: Int) {
        val randomShape = EnemyShape.values().random()


        val enemy = Enemy(
            screenWidth,
            screenHeight,
            lane,
            startY = -ENEMY_SIZE,
            speed = fixedSpeed,
            shape = randomShape
        )
        enemiesMutable.add(enemy)
    }

    private fun spawnPattern() {
        val r = Random.nextFloat()

        val twoEnemyProbability = when (mode) {
            SpawnMode.NORMAL -> 0.55f
            SpawnMode.HARD -> 0.65f
            SpawnMode.EXTREME -> 0.75f
        }

        if (r <= (1f - twoEnemyProbability)) {
            val lane = Random.nextInt(0, 3)
            spawnEnemy(lane)
        } else {
            val free = Random.nextInt(0, 3)
            for (lane in 0..2) {
                if (lane != free) spawnEnemy(lane)
            }
        }
    }

    fun getEnemies(): List<Enemy> = enemiesMutable

    fun removeEnemy(enemy: Enemy) {
        enemiesMutable.remove(enemy)
    }

    fun clear() {
        enemiesMutable.clear()
        spawnTimer = 0f
        elapsedTime = 0f
    }

    fun getDebugInfo(): String {
        return "Speed: ${fixedSpeed.toInt()}px/s (FIXED) | Spawn: ${"%.2f".format(spawnInterval)}s | Mode: ${mode.name}"
    }
}

class CollisionSystem {
    fun checkCollision(playerX: Float, playerY: Float, enemy: Enemy, threshold: Float = 45f): Boolean {
        val dx = enemy.x - playerX
        val dy = enemy.y - playerY
        val dist = hypot(dx.toDouble(), dy.toDouble())

        val adjustedThreshold = threshold + (enemy.speed * 0.015f)
        return dist < adjustedThreshold
    }
}

class ScoreSystem {
    var score: Int = 0
        private set
    private var timeSeconds: Float = 0f
    val pointsPerSecond = 15f

    fun update(deltaTime: Float) {
        timeSeconds += deltaTime
        score = (timeSeconds * pointsPerSecond).toInt()
    }

    fun getTime(): Int = timeSeconds.toInt()

    fun getFormattedTime(): String {
        val total = getTime()
        val mm = total / 60
        val ss = total % 60
        return String.format("%02d:%02d", mm, ss)
    }

    fun reset() {
        timeSeconds = 0f
        score = 0
    }
}