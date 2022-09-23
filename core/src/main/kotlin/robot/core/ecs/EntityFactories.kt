package robot.core.ecs

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import eater.ai.AiComponent
import eater.ai.GenericAction
import eater.ai.GenericActionWithState
import eater.core.engine
import eater.core.world
import eater.ecs.components.Box2d
import eater.ecs.components.CameraFollow
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.EngineEntity
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.with
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.math.minus
import ktx.math.vec2
import robot.core.ecs.components.Car
import robot.core.ecs.components.Player
import robot.core.ecs.components.Robot
import robot.core.ecs.components.SpriteComponent
import robot.core.track.TrackMania

object RobotActions {
    private val robotFamily = allOf(Robot::class).get()
    private val allTheRobots get() = engine().getEntitiesFor(robotFamily)

    /**
     * Aah, I just figured it out:
     *
     * The track of points that we have that are the "center" of the track
     * can be used to guid the enemy cars. They could try to drive as fast as they can towards
     * the next center point and everything is good.
     *
     * Or the player. We can simply check if the player is to the left or right of the forward
     * normal of the car and if it is, we turn left
     *
     *
     */
    val trackMania = inject<TrackMania>()
    val chaseMiddle = GenericActionWithState<Robot>("Chase Player", {
        1f
    }, {

    }, { entity, robot, deltaTime ->

        /**
         * get the next point to move towards. It should simply be some point with a higher y than the current
         * cars position
         *
         * No, we keep track of an index of course
         */
        val body = Box2d.get(entity).body
        if(robot.target.y < body.worldCenter.y) {
            robot.targetIndex = trackMania.getNextTarget(robot.targetIndex, body.worldCenter.y, robot.target)
        }
        val targetDirection = (robot.target - body.worldCenter)

    })

}

fun createRobotCar(position: Vector2, width: Float, height: Float): Entity {
    return engine().entity {
        carEntity(this, position, width, height)
        with<Robot>()
        with<AiComponent> {

        }
    }
}

fun createPlayerEntity(position: Vector2, width: Float, height: Float): Entity {
    /**
     * The player entity shall have
     * a sprite, a physical car body,
     * perhaps some wheels?
     */
    return engine().entity {
        carEntity(this, position, width, height)
        with<Player>()
        with<CameraFollow>()
    }
}

fun carEntity(entity: EngineEntity, worldPos: Vector2, width: Float, height: Float) {

    entity.apply {
        with<Box2d> {
            body = world().body {
                userData = this@apply
                type = com.badlogic.gdx.physics.box2d.BodyDef.BodyType.DynamicBody
                position.set(worldPos)
                box(width, height) {
                    density = 0.5f
                }
                circle(10f, vec2(0f, 0f)) {
                    isSensor = true
                }
            }
        }
        with<Car>()
        with<SpriteComponent> {
            texture = robot.core.Assets.blueCarRegion
            shadow = robot.core.Assets.carShadowRegion
        }
    }
}
