package robot.core.ecs

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.BodyDef
import eater.core.engine
import eater.core.world
import eater.ecs.components.Box2d
import eater.ecs.components.CameraFollow
import eater.ecs.components.TransformComponent
import ktx.ashley.entity
import ktx.ashley.with
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.math.vec2
import robot.core.Assets
import robot.core.ecs.components.Car
import robot.core.ecs.components.SpriteComponent

fun createPlayerEntity(width: Float, height: Float): Entity {
    /**
     * The player entity shall have
     * a sprite, a physical car body,
     * perhaps some wheels?
     */
    return engine().entity {
        with<Box2d> {
            body = world().body {
                userData = this@entity.entity
                type = BodyDef.BodyType.DynamicBody
                position.set(0f, 0f)
                box(width, height) {
                    density = 0.5f
                }
                circle(10f, vec2(0f,0f)) {
                    isSensor = true
                }
            }
        }
        with<CameraFollow>()
        with<Car>()
        with<SpriteComponent> {
            texture = Assets.blueCarRegion.first()
        }
    }
}