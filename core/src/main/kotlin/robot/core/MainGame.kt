package robot.core

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Queue
import eater.core.MainGame
import eater.ecs.components.Box2d
import robot.core.GameConstants.MinRobots
import robot.core.ecs.ExplosionData
import robot.core.injection.Context
import robot.core.screens.GameScreen
import robot.core.screens.StartScreen

object GameState {
    var raceStarted = false
    var fillUpRobotsDelay = 5f
    var startCountDown = 3f
    var score = 0
    var highScore = 0
        get() {
            if(score > field)
                field = score
            return field
        }
    var timesPlayed = 0
    var gameStarted = false
    var playerDied = false
    var playerWon = false
    var minRobots = MinRobots
    lateinit var playerEntity: Entity
    val playerReady get() = ::playerEntity.isInitialized && Box2d.has(playerEntity)

    fun start() {
        minRobots = MinRobots
        score = 0
        fillUpRobotsDelay = 5f
        raceStarted = false
        playerDied = false
        playerWon = false
        gameStarted = true
        timesPlayed++
        startCountDown = 3f
        explosionQueue.clear()
    }

    fun playerDied() {
        playerDied = true
    }
    fun playerWon() {
        playerWon = true
    }

    val explosionQueue = Queue<ExplosionData>()

}

class RoboGame : MainGame() {
    override fun create() {
        Context.initialize()
        addScreen(StartScreen(this))
        addScreen(GameScreen(this))
        setScreen<StartScreen>()
    }

    fun startGame() {
        GameState.start()
        setScreen<GameScreen>()
    }

    fun playerDied() {
        setScreen<StartScreen>()
    }
    fun playerWon() {
        setScreen<StartScreen>()
    }
}

