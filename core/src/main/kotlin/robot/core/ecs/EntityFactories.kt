package robot.core.ecs

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

fun createPlayerEntity() {
    /**
     * The player entity shall have
     * a sprite, a physical car body,
     * perhaps some wheels?
     */
    engine().entity {
        with<Box2d> {
            body = world().body {
                userData = this@entity.entity
                type = BodyDef.BodyType.DynamicBody
                position.set(0f, 0f)
                box(2f, 4f) {
                    friction = 10f //Tune
                    density = 1f //tune
//                    filter {
//                        categoryBits = Box2dCategories.cities
//                        maskBits = Box2dCategories.whatCitiesCollideWith
//                    }
                }
            }
        }
        with<TransformComponent>()
        with<CameraFollow>()
    }
}