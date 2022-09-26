package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import eater.ecs.components.Box2d
import ktx.ashley.allOf
import robot.core.ecs.components.Car
import robot.core.ecs.components.Robot

class RobotCarSpeedAndStuffSystem: IteratingSystem(allOf(Robot::class, Car::class, Box2d::class).get()) {
    override fun processEntity(entity: Entity?, deltaTime: Float) {
        /*

        What do we do?

        We check if the player is in front or behind of this particular robot car.

        If it is in front of the car, we speed up

        If it is behind the car, we lower top speed.
         */
    }

}