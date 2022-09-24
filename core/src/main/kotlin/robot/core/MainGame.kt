package robot.core

import eater.core.MainGame
import ktx.app.KtxGame
import ktx.app.KtxScreen
import robot.core.injection.Context
import robot.core.screens.GameScreen
import robot.core.screens.StartScreen

object GameState {
    var score = 0
}

class RoboGame : MainGame() {
    override fun create() {
        Context.initialize()
        addScreen(StartScreen(this))
        addScreen(GameScreen(this))
        setScreen<StartScreen>()
    }

    fun startGame() {
        setScreen<GameScreen>()
    }
}

