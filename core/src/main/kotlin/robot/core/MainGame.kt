package robot.core

import eater.core.MainGame
import robot.core.injection.Context
import robot.core.screens.GameScreen
import robot.core.screens.StartScreen

object GameState {
    var startCountDown = 3f
    var score = 0
    var highScore = 0
        get() {
            if(score > field)
                field = score
            return field
        }
    var timesPlayed = 1
    var gameStarted = false
    var playerDied = false
    var playerWon = false

    fun start() {
        score = 0
        playerDied = false
        playerWon = false
        gameStarted = true
        timesPlayed++
        startCountDown = 3f
    }

    fun playerDied() {
        playerDied = true
    }
    fun playerWon() {
        playerWon = true
    }

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

