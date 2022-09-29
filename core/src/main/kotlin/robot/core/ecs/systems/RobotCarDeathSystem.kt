package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import eater.ecs.components.Box2d
import eater.physics.addComponent
import ktx.ashley.allOf
import robot.core.ecs.components.Car
import eater.ecs.components.Remove
import robot.core.ecs.components.Robot
import robot.core.ecs.explosionLater

class RobotCarDeathSystem: IteratingSystem(allOf(Car::class, Robot::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if(Car.get(entity).health < 0f) {
            entity.addComponent<Remove>()
            explosionLater(Box2d.get(entity).body.worldCenter, 15f, 25f)
        }
    }
}

