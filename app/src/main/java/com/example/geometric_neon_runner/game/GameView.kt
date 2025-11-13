package com.example.geometric_neon_runner.game

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.geometric_neon_runner.game.entities.Enemy
import com.example.geometric_neon_runner.game.entities.Player
import com.example.geometric_neon_runner.game.render.GameRenderer
import com.example.geometric_neon_runner.game.systems.CollisionSystem
import com.example.geometric_neon_runner.game.systems.ScoreSystem
import com.example.geometric_neon_runner.game.systems.SpawnMode
import com.example.geometric_neon_runner.game.systems.SpawnSystem
import kotlin.math.abs

class GameView(context: Context, private val mode: SpawnMode = SpawnMode.NORMAL) :
    SurfaceView(context), SurfaceHolder.Callback {

    private lateinit var player: Player
    private lateinit var spawnSystem: SpawnSystem
    private val enemies = mutableListOf<Enemy>()
    private val collisionSystem = CollisionSystem()
    private val scoreSystem = ScoreSystem()
    private lateinit var renderer: GameRenderer
    private lateinit var gameLoop: GameLoop

    var gameState: GameState = GameState.Playing

    var onGameOver: ((score: Int, time: Int) -> Unit)? = null
    var onScoreChanged: ((score: Int) -> Unit)? = null

    private var downX = 0f
    private var downY = 0f
    private var isDragging = false

    private var screenWidth = 0
    private var screenHeight = 0

    init {
        holder.addCallback(this)
    }

    private fun initSystems() {
        screenWidth = width.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels
        screenHeight = height.takeIf { it > 0 } ?: resources.displayMetrics.heightPixels

        player = Player(screenWidth, screenHeight, initialLane = 1)
        spawnSystem = SpawnSystem(screenWidth, screenHeight, mode)
        renderer = GameRenderer(screenWidth, screenHeight)
        scoreSystem.reset()
        enemies.clear()

        gameLoop = GameLoop(this)
    }

    fun startGame() {
        if (!::player.isInitialized) initSystems()
        gameState = GameState.Playing
        gameLoop.start()
    }

    fun stopGame() {
        if (::gameLoop.isInitialized) {
            gameLoop.stop()
        }
    }

    fun update(deltaTime: Float) {
        if (gameState != GameState.Playing) return

        player.update(deltaTime)
        spawnSystem.update(deltaTime)

        val spawned = spawnSystem.getEnemies()
        synchronized(enemies) {
            for (s in spawned) {
                if (!enemies.contains(s)) enemies.add(s)
            }

            val iter = enemies.iterator()
            while (iter.hasNext()) {
                val e = iter.next()
                e.update(deltaTime)


                if (e.y > screenHeight + 200f) {
                    iter.remove()
                    spawnSystem.removeEnemy(e)
                }
            }

            while (enemies.size > 60) {
                val oldest = enemies.firstOrNull()
                if (oldest != null) {
                    enemies.remove(oldest)
                    spawnSystem.removeEnemy(oldest)
                }
            }
        }

        renderer.update(deltaTime)
        scoreSystem.update(deltaTime)
        onScoreChanged?.invoke(scoreSystem.score)

        checkCollisions()
    }

    fun render() {
        val canvas: Canvas? = holder.lockCanvas()
        if (canvas != null) {
            try {
                canvas.drawRGB(0, 0, 0)
                renderer.render(canvas, player, synchronizedListCopy())
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    private fun synchronizedListCopy(): List<Enemy> {
        synchronized(enemies) {
            return enemies.toList()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                isDragging = true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - downX
                val dy = event.y - downY
                if (isDragging && abs(dx) > 40 && abs(dx) > abs(dy)) {
                    handleInput(downX, dx)
                    isDragging = false
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    val dx = event.x - downX
                    if (abs(dx) < 20) {
                        handleTap(event.x)
                    } else {
                        handleInput(downX, dx)
                    }
                }
                isDragging = false
            }
        }
        return true
    }

    private fun handleTap(x: Float) {
        val third = width / 3f
        val lane = when {
            x < third -> 0
            x < third * 2 -> 1
            else -> 2
        }
        player.moveToLane(lane)
    }

    private fun handleInput(startX: Float, deltaX: Float) {
        if (deltaX < 0) {
            player.moveToLane(player.currentLane - 1)
        } else {
            player.moveToLane(player.currentLane + 1)
        }
    }

    private fun checkCollisions() {
        val plX = player.x
        val plY = player.y
        val threshold = 50f

        synchronized(enemies) {
            for (e in enemies) {
                if (collisionSystem.checkCollision(plX, plY, e, threshold)) {
                    gameState = GameState.GameOver
                    onGameOver?.invoke(scoreSystem.score, scoreSystem.getTime())
                    gameLoop.stop()
                    break
                }
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (!::player.isInitialized) initSystems()
        gameLoop.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGame()
    }
}