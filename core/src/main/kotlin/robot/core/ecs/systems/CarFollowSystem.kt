package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import eater.ecs.components.Box2d
import eater.ecs.components.CameraFollow
import eater.physics.addComponent
import ktx.ashley.allOf
import ktx.ashley.remove
import robot.core.ecs.components.Car

class CarFollowSystem: IteratingSystem(allOf(Car::class).get()) {
    private var highestCar: Entity? = null
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if(highestCar != null && Box2d.has(highestCar!!)) {
            if(entity != highestCar) {
                if(Box2d.has(entity) && Box2d.get(entity).body.worldCenter.y > Box2d.get(highestCar!!).body.worldCenter.y) {
                    highestCar?.remove<CameraFollow>()
                    entity.addComponent<CameraFollow>()
                    highestCar = entity
                }
            }
        } else {
            highestCar = entity
        }
    }

}