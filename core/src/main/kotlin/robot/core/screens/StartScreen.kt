package robot.core.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import eater.core.BasicScreen
import eater.injection.InjectionContext.Companion.inject
import eater.input.CommandMap
import ktx.actors.alpha
import ktx.actors.stage
import ktx.graphics.use
import ktx.math.vec2
import ktx.scene2d.*
import robot.core.Assets
import robot.core.GameState
import robot.core.RoboGame
import robot.core.ui.boundLabel
import robot.core.ui.boundProgressBar
import space.earlygrey.shapedrawer.ShapeDrawer

class StartScreen(private val roboGame: RoboGame) : BasicScreen(
    roboGame,
    CommandMap("StartScreen").apply {
        setDown(Input.Keys.ENTER, "Start Game") { roboGame.startGame() }
    }
) {


    override val viewport: Viewport = ExtendViewport(512f, 512f)
    override fun keyDown(keycode: Int): Boolean {
        roboGame.startGame()
        return true
    }

    private val stage by lazy {
        stage(batch, viewport).apply {
            actors {
                image(splashBackground) {
                    width = stage.width
                    height = stage.height

                }
                table {

                }


                val currentPos = vec2(this@apply.width / 2f - 150f, this@apply.height / 2f + 100f)
                label("Created by a Robot Takeover") {
                    setFontScale(2.5f)
                    setPosition(currentPos.x, currentPos.y)
                    currentPos.y -= this.height * 2
                }
                """
Race

Win - and humanity is allowed to live

Lose - and there is no future

                   """.trimIndent()
                    .split("\n")
                    .forEach {
                        label(it) {
                            setFontScale(1f)
                            setPosition(currentPos.x, currentPos.y)
                            currentPos.y -= this.height * 2
                        }
                    }
                currentPos.x = currentPos.x + 50f
                """
Controls
WASD - control your car
Space - fire current weapon
Any Key - Start Game
                   """.trimIndent()
                    .split("\n")
                    .forEach {
                        label(it) {
                            setFontScale(1f)
                            setPosition(currentPos.x, currentPos.y)
                            currentPos.y -= this.height * 2
                        }
                    }
                if (GameState.timesPlayed > 0) {
                    boundLabel({ "Score: ${GameState.score}" }) {
                        setFontScale(1f)
                        setPosition(currentPos.x, currentPos.y)
                        currentPos.y -= this.height * 2
                    }
                    boundLabel({ "Hi-Score: ${GameState.highScore}" }) {
                        setFontScale(1f)
                        setPosition(currentPos.x, currentPos.y)
                        currentPos.y -= this.height * 2
                    }
                }


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


    override fun show() {
        Scene2DSkin.defaultSkin = Skin(Gdx.files.internal("ui/uiskin.json"))
        super.show()
    }

    val splashBackground = Assets.splashBackground
    val shapeDrawer by lazy { inject<ShapeDrawer>() }
    override fun render(delta: Float) {
        super.render(delta)
//        batch.use {
//            batch.draw(splashBackground, 0f, 0f, viewport.worldWidth, viewport.worldHeight)
//        }
        stage.act(delta)
        stage.draw()
    }
}