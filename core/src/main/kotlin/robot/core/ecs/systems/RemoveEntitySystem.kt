package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import eater.core.world
import eater.ecs.components.Box2d
import ktx.ashley.allOf
import robot.core.ecs.components.Death

class RemoveEntitySystem: IteratingSystem(allOf(Death::class).get()) {
    val toRemove = mutableListOf<Entity>()
    val world by lazy { world() }
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        for(entity in toRemove) {

            world.destroyBody(Box2d.get(entity).body)
            engine.removeEntity(entity)
        }
        toRemove.clear()
    }
    override fun processEntity(entity: Entity, deltaTime: Float) {
        toRemove.add(entity)
    }

}