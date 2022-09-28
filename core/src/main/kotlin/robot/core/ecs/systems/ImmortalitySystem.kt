package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.allOf
import robot.core.ecs.components.Car

class ImmortalitySystem: IteratingSystem(allOf(Car::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        Car.get(entity).immortalTimer -= deltaTime
    }
}