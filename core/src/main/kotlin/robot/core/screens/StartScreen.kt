package robot.core.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import eater.core.BasicScreen
import eater.injection.InjectionContext.Companion.inject
import eater.input.CommandMap
import ktx.actors.stage
import ktx.graphics.use
import ktx.math.vec2
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actors
import ktx.scene2d.label
import robot.core.Assets
import robot.core.RoboGame
import space.earlygrey.shapedrawer.ShapeDrawer

class StartScreen(mainGame: RoboGame) : BasicScreen(
    mainGame,
    CommandMap("StartScreen").apply {
        setDown(Input.Keys.ENTER, "Start Game") { mainGame.startGame() }
    }
) {


    private val stage by lazy {
        stage(batch, viewport).apply {
            actors {
                val currentPos = vec2(this@apply.width / 2f - 50f, this@apply.height / 2f - 50f)
                label("Created by a Robot Takeover") {
                    setFontScale(1f)
                    setPosition(currentPos.x, currentPos.y)
                    currentPos.y -= this.height * 2
                    currentPos.x -= 100f
//                }
//                label("GAME OVER") {
//                    setFontScale(2f)
//                    setPosition(currentPos.x, currentPos.y)
//                    currentPos.y -= this.height * 2
//                }
//                label("Fish the fish with your floating city's nets (theme, you know)") {
//                    setFontScale(0.6f)
//                    setPosition(currentPos.x, currentPos.y)
//                    currentPos.y -= this.height * 2f
//                }
//                label("Steer the sail with A and D - ") {
//                    setFontScale(0.6f)
//                    setPosition(currentPos.x, currentPos.y)
//                    currentPos.y -= this.height * 2f
//                }
//                label("the white dial shows wind direction") {
//                    setFontScale(0.6f)
//                    setPosition(currentPos.x, currentPos.y)
//                    currentPos.y -= this.height * 7f
//                }
//                label("""You caught ${GameStats.caughtFish} fish
//                    You maxed your population at ${GameStats.maxPopulation}
//                    The game ended when you had ${GameStats.remainingFood} food remainging.
//                    You played for ${GameStats.playTime.toInt()} seconds
//                    Longest playtime is ${GameStats.highestPlayTime.toInt()} seconds
//                """.trimMargin()){
//                    setFontScale(0.6f)
//                    setPosition(currentPos.x, currentPos.y)
//                    currentPos.y -= this.height * 2f
//                }


                }
            }
        }
    }

    override fun show() {
        Scene2DSkin.defaultSkin = Skin(Gdx.files.internal("ui/uiskin.json"))
        super.show()
    }

    val splashBackground = Assets.splashBackground
    val shapeDrawer by lazy { inject<ShapeDrawer>() }
    override fun render(delta: Float) {
        super.render(delta)
        batch.use {
            batch.draw(splashBackground,  0f, 0f, viewport.worldWidth, viewport.worldHeight)
        }
        stage.act(delta)
        stage.draw()
    }
}