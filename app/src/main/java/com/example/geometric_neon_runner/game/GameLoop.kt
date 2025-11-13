package com.example.geometric_neon_runner.game

class GameLoop(private val gameView: GameView) : Runnable {
    @Volatile
    private var running = false
    private var thread: Thread? = null

    fun start() {
        if (running) return
        running = true
        thread = Thread(this, "GameLoopThread")
        thread?.start()
    }

    fun stop() {
        running = false
        try {
            thread?.join(2000)
        } catch (e: InterruptedException) {
            // ignorar
        } finally {
            thread = null
        }
    }

    override fun run() {
        var lastTime = System.nanoTime()
        while (running) {
            val currentTime = System.nanoTime()
            var deltaTime = (currentTime - lastTime) / 1_000_000_000f
            lastTime = currentTime


            if (deltaTime > 0.033f) {
                deltaTime = 0.033f
            }

            if (deltaTime < 0.001f) {
                deltaTime = 0.001f
            }

            try {
                gameView.update(deltaTime)
                gameView.render()
            } catch (t: Throwable) {
                t.printStackTrace()
            }

            val frameTime = (System.nanoTime() - currentTime) / 1_000_000
            val targetFrameTime = 16L
            val sleepTime = targetFrameTime - frameTime

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime)
                } catch (ie: InterruptedException) {
                }
            }
        }
    }
}