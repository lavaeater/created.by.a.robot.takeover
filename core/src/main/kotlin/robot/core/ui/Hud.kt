package robot.core.ui

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Align
import eater.core.engine
import eater.ecs.components.Box2d
import eater.extensions.boundLabel
import eater.extensions.boundProgressBar
import eater.injection.InjectionContext.Companion.inject
import eater.physics.forwardNormal
import eater.physics.forwardVelocity
import eater.ui.LavaHud
import ktx.actors.stage
import ktx.ashley.allOf
import ktx.scene2d.*
import robot.core.GameState
import robot.core.ecs.components.Car
import robot.core.ecs.components.Player
import robot.core.track.TrackMania

class Hud(batch: PolygonSpriteBatch) : LavaHud(batch) {
    private val playerFamily = allOf(Player::class).get()
    private val playerEntities get() = engine().getEntitiesFor(playerFamily)
    private val speed
        get() = if (Box2d.has(GameState.playerEntity))
            (Box2d.get(GameState.playerEntity).body.forwardVelocity()
                .dot(Box2d.get(GameState.playerEntity).body.forwardNormal()) * 2).toInt() else 0

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
                if (car.lastPickup != null)
                    car.lastPickup!!.name
                else
                    ""
            } else ""
        }

    private val shield: String
        get() {
            return if(Car.has(GameState.playerEntity)) {
                val car = Car.get(GameState.playerEntity)
                if(car.immortal) {
                    "Shield: ${car.immortalTimer.toInt()} s"
                } else "Shield: No"
            } else "Shield: No"
        }
    private val weapons: String
        get() {
            return if (Car.has(GameState.playerEntity)) {
                val car = Car.get(GameState.playerEntity)
                if (car.currentWeapon != null)
                    car.currentWeapon!!.name
                else
                    "No weapons"
            } else "No weapons"
        }

    override val stage: Stage by lazy {
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
                    label("Current Weapon")
                    boundLabel({ weapons })
                    label("Last pickup")
                    boundLabel({ lastPickup })
                    boundLabel({ shield })
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

}