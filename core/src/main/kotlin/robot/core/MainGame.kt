package robot.core

import ktx.app.KtxGame
import ktx.app.KtxScreen

class MainGame : KtxGame<KtxScreen>() {
    override fun create() {
        addScreen(FirstScreen(this))
        setScreen<FirstScreen>()
    }
}

