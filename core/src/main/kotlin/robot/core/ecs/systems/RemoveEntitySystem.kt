package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import eater.core.world
import eater.ecs.components.Box2d
import eater.ecs.components.CameraFollow
import ktx.ashley.allOf
import ktx.ashley.remove
import robot.core.ecs.components.Remove

class RemoveEntitySystem: IteratingSystem(allOf(Remove::class).get()) {
    val toRemove = mutableListOf<Entity>()
    val world by lazy { world() }
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        for(entity in toRemove) {
            if(CameraFollow.has(entity)) {
                entity.remove<CameraFollow>()
            }
            val body = Box2d.get(entity).body
            entity.remove<Box2d>()
            world.destroyBody(body)
            engine.removeEntity(entity)
        }
        toRemove.clear()
    }
    override fun processEntity(entity: Entity, deltaTime: Float) {
        toRemove.add(entity)
    }

}