package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import eater.physics.addComponent
import ktx.ashley.allOf
import robot.core.ecs.components.Car
import robot.core.ecs.components.Death

class CarDeathSystem: IteratingSystem(allOf(Car::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if(Car.get(entity).health < 0f)
            entity.addComponent<Death>()
    }
}