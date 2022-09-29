package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import eater.ecs.components.Box2d
import eater.physics.addComponent
import ktx.ashley.allOf
import robot.core.GameConstants
import robot.core.GameState
import robot.core.ecs.components.Car
import eater.ecs.components.Remove
import robot.core.ecs.components.Robot

class RobotAnnihilationSystem: IteratingSystem(allOf(Robot::class, Car::class, Box2d::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if(GameState.raceStarted && Box2d.has(GameState.playerEntity)) {
            val robotBody = Box2d.get(entity).body

            val pPos = Box2d.get(GameState.playerEntity).body.worldCenter
            val rPos = robotBody.worldCenter

            if (pPos.dst(rPos) > GameConstants.RobotMaxDistance)
                entity.addComponent<Remove>()
        }
    }
}