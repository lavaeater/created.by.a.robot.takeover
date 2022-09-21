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
import robot.core.ecs.components.Car

fun createPlayerEntity(): Entity {
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
                box(4f, 8f) {
                    friction = 0.1f //Tune
                    density = 0.1f //tune

//                    filter {
//                        categoryBits = Box2dCategories.cities
//                        maskBits = Box2dCategories.whatCitiesCollideWith
//                    }
                }
                circle(10f, vec2(0f,0f)) {
                    isSensor = true
                }
            }
        }
        with<CameraFollow>()
        with<Car>()
    }
}