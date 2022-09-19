package robot.core

import eater.core.MainGame

class MainGame : MainGame() {
    override fun create() {
        addScreen(FirstScreen(this))
        setScreen<FirstScreen>()
    }
}

