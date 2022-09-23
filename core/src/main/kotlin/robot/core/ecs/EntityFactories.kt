package robot.core.ecs

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import eater.ai.AiComponent
import eater.core.engine
import eater.core.world
import eater.ecs.components.Box2d
import eater.ecs.components.CameraFollow
import ktx.ashley.EngineEntity
import ktx.ashley.entity
import ktx.ashley.with
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.math.vec2
import robot.core.ecs.components.Car
import robot.core.ecs.components.Player
import robot.core.ecs.components.Robot
import robot.core.ecs.components.SpriteComponent

fun createRobotCar(position: Vector2, width: Float, height: Float): Entity {
    return engine().entity {
        carEntity(this, position, width, height, UserData.Robot(this.entity))
        with<Robot>()
        with<AiComponent> {
            actions.add(RobotActions.chaseMiddle)
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
        carEntity(this, position, width, height, UserData.Robot(this.entity))
        with<Player>()
        with<CameraFollow>()
    }
}

fun carEntity(entity: EngineEntity, worldPos: Vector2, width: Float, height: Float, ud:UserData) {

    entity.apply {
        with<Box2d> {
            body = world().body {
                userData = ud
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

sealed class UserData {
    object Wall: UserData()
    class Player(val entity: Entity):UserData()
    class Robot(val entity: Entity):UserData()
}
