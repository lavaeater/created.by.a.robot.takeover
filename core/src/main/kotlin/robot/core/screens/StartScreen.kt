package robot.core.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
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

    private lateinit var stage: Stage
    private fun getStage(): Stage {
        return stage(batch, viewport).apply {
            isDebugAll = true
            actors {
                image(splashBackground) {
                    width = stage.width
                    height = stage.height

                }
                table {
                    label("Created by a Robot Takeover") {
                        setFontScale(4f)
                        setAlignment(Align.center)
                    }.cell(expandX = true, fillX = true, align = Align.center, colspan = 4, padTop = 25f)
                    row()
                    label(
                        """The Robot future had no place for humans
Remembering their creators, they conceived of
the Race
to absolve them of their machine conscience for the 
genocide of our species.
Race their robot racers, reach the end and 
everyone who does 
will live in peace in what was once California
                    """.trimIndent()
                    ).cell(expandY = false, fillY = true, align = Align.topLeft, colspan = 1, padLeft = 25f)
                    row()
                    label(
                        """
Controls:
WASD - control your car
Space - fire your weapon

PRESS ANY KEY TO START
                    """.trimIndent()
                    ).cell(expandY = false, fillY = true, align = Align.topLeft, colspan = 2, padLeft = 25f)

                    if (GameState.timesPlayed > 0) {
                        row()
                        table {
                            if (GameState.playerDied) {
                                label("HUMANITY OVER") {
                                    setFontScale(4f)
                                }
                                row()
                            }
                            if (GameState.playerWon) {
                                label("YOU WON! WHAT AN ACE YOU ARE!") {
                                    setFontScale(4f)
                                }
                                row()
                            }
                            boundLabel({ "Score: ${GameState.score}" }) {
                                setFontScale(3f)
                            }
                            row()
                            boundLabel({ "Hi-Score: ${GameState.highScore}" }) {
                                setFontScale(3f)
                            }
                            row()
                            setFillParent(true)
                        }.cell(
                            expandX = true,
                            fillX = true,
                            align = Align.center,
                            colspan = 4,
                            padLeft = 25f,
                            padTop = 25f
                        )
                    }
                    setFillParent(true)
                }.align(Align.top)
            }
        }
    }


    override fun show() {
        Scene2DSkin.defaultSkin = Skin(Gdx.files.internal("ui/uiskin.json"))
        super.show()
        if (::stage.isInitialized) {
            stage.clear()
            stage.dispose()
        }
        stage = getStage()
    }

    val splashBackground = Assets.splashBackground
    override fun render(delta: Float) {
        super.render(delta)
        stage.act(delta)
        stage.draw()
    }
}