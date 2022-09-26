package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.physics.box2d.Body
import eater.ecs.components.Box2d
import eater.physics.addComponent
import ktx.ashley.allOf
import robot.core.GameConstants
import robot.core.ecs.components.Car
import robot.core.ecs.components.Player
import robot.core.ecs.components.Remove
import robot.core.ecs.components.Robot

class RobotAnnihilationSystem: IteratingSystem(allOf(Robot::class, Car::class, Box2d::class).get()) {
    val playerFamily = allOf(Player::class).get()
    val players get() = engine.getEntitiesFor(playerFamily)
    var playerBody: Body? = null
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (playerBody == null && players.any() && Box2d.has(players.first()))
            playerBody = Box2d.get(players.first()).body

        val robotBody = Box2d.get(entity).body

        val pPos = playerBody!!.worldCenter
        val rPos = robotBody.worldCenter

        if(pPos.dst(rPos) > GameConstants.RobotMaxDistance)
            entity.addComponent<Remove>()
    }
}