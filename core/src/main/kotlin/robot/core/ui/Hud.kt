package robot.core.ui

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import eater.core.engine
import eater.ecs.components.Box2d
import eater.ecs.components.CameraFollow
import eater.injection.InjectionContext.Companion.inject
import eater.physics.forwardNormal
import eater.physics.forwardVelocity
import ktx.actors.stage
import ktx.actors.txt
import ktx.ashley.allOf
import ktx.graphics.use
import ktx.math.*
import ktx.scene2d.*
import robot.core.Assets
import robot.core.GameState
import robot.core.ecs.components.Car
import robot.core.ecs.components.Player
import robot.core.ecs.components.Robot
import robot.core.track.TrackMania
import space.earlygrey.shapedrawer.ShapeDrawer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class Hud(private val batch: PolygonSpriteBatch, debugAll: Boolean = false) {
    private val aspectRatio = 16f / 9f
    private val hudWidth = 180f
    private val hudHeight = hudWidth * aspectRatio

    private val camera = OrthographicCamera()
    private val hudViewPort = ExtendViewport(hudWidth, hudHeight, camera)
    private val worldCamera by lazy { inject<OrthographicCamera>() }

    private val projectionVector = vec3()
    private val _projectionVector = vec2()
    private val projection2d: Vector2
        get() {
            _projectionVector.set(projectionVector.x, projectionVector.y)
            return _projectionVector
        }

    private val robotFamily = allOf(Robot::class).get()
    private val robots get() = engine().getEntitiesFor(robotFamily)
    private val robotCount get() = robots.size()

    private val playerFamily = allOf(Player::class).get()
    private val playerEntities get() = engine().getEntitiesFor(playerFamily)
    private val speed
        get() = if (Box2d.has(GameState.playerEntity))
            (Box2d.get(GameState.playerEntity).body.forwardVelocity()
                .dot(Box2d.get(GameState.playerEntity).body.forwardNormal()) * 2).toInt() else 0
//            (Box2d.get(GameState.playerEntity).body.linearVelocity.len() * 2).toInt() else 0

    private var topSpeed = 0
        get() {
            if (speed > field)
                field += 10
            return field
        }

    private val trackMania by lazy { inject<TrackMania>() }

    private val progress: Float
        get() {
            return if (Box2d.has(GameState.playerEntity)) {
                val index = trackMania.getIndexForPosition(Box2d.get(GameState.playerEntity).body.worldCenter.y)
                val normalized = MathUtils.norm(0f, trackMania.track.size.toFloat(), index.toFloat())
                GameState.score = (normalized * 100).toInt()
                normalized
            } else 0f
        }

    private val lastPickup: String
        get() {
            return if (Car.has(GameState.playerEntity)) {
                val car = Car.get(GameState.playerEntity)
                if (car.weapons.any())
                    car.weapons.last().name
                else
                    ""
            } else ""
        }
    private val weapons: String
        get() {
            return if (Car.has(GameState.playerEntity)) {
                val car = Car.get(GameState.playerEntity)
                if (car.weapons.any())
                    car.weapons.first().name
                else
                    "No weapons"
            } else "No weapons"
        }

    val stage by lazy {
        val aStage = stage(batch, hudViewPort)
        aStage.actors {
            table {
                verticalGroup {
                    label("Progress")
                    boundProgressBar({ progress }, 0f, 1f)
                }.cell(align = Align.bottomLeft, grow = true, fill = true, pad = 10f)

                verticalGroup {
                    label("Health")
                    boundProgressBar(
                        { if (playerEntities.any()) Car.get(playerEntities.first()).health else 0f },
                        0f,
                        100f,
                        1f
                    )
                }.cell(align = Align.bottomLeft, grow = true, fill = true, pad = 10f)
                verticalGroup {
                    label("Next Weapon")
                    boundLabel({ weapons })
                    label("Last pickup")
                    boundLabel({ lastPickup })
                }.cell(align = Align.bottomLeft, grow = true, fill = true, pad = 10f)
                verticalGroup {
                    horizontalGroup {
                        label("Speed: ")
                        boundLabel({ "$speed km/h" })
                    }
                    horizontalGroup {
                        label("Top Speed: ")
                        boundLabel({ "$topSpeed km/h" })
                    }
                }.cell(align = Align.bottomRight, grow = true, fill = true, pad = 10f)
                pack()
            }
        }
        aStage
    }

    private val shapeDrawer by lazy { inject<ShapeDrawer>() }

    fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
    }
}


@Scene2dDsl
@OptIn(ExperimentalContracts::class)
inline fun <S> KWidget<S>.boundLabel(
    noinline textFunction: () -> String,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: (@Scene2dDsl BoundLabel).(S) -> Unit = {}
): Label {
    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
    return actor(BoundLabel(textFunction, skin), init)
}


open class BoundLabel(private val textFunction: () -> String, skin: Skin = Scene2DSkin.defaultSkin) :
    Label(textFunction(), skin) {
    override fun act(delta: Float) {
        txt = textFunction()
        super.act(delta)
    }
}

open class BoundProgressBar(
    private val valueFunction: () -> Float,
    min: Float,
    max: Float,
    stepSize: Float,
    skin: Skin = Scene2DSkin.defaultSkin
) : ProgressBar(min, max, stepSize, false, skin) {
    override fun act(delta: Float) {
        value = valueFunction()
        super.act(delta)
    }
}

@Scene2dDsl
@OptIn(ExperimentalContracts::class)
inline fun <S> KWidget<S>.boundProgressBar(
    noinline valueFunction: () -> Float,
    min: Float = 0f,
    max: Float = 1f,
    step: Float = 0.01f,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: (@Scene2dDsl BoundProgressBar).(S) -> Unit = {}
): BoundProgressBar {
    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
    return actor(BoundProgressBar(valueFunction, min, max, step, skin), init)
}


//@Scene2dDsl
//@OptIn(ExperimentalContracts::class)
//inline fun <S> KWidget<S>.typingLabel(
//    text: CharSequence,
//    style: String = defaultStyle,
//    skin: Skin = Scene2DSkin.defaultSkin,
//    init: (@Scene2dDsl TypingLabel).(S) -> Unit = {}
//): TypingLabel {
//    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
//    return actor(TypingLabel(text, skin, style), init)
//}