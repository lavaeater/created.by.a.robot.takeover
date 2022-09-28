package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import eater.ecs.components.Box2d
import eater.physics.addComponent
import ktx.ashley.allOf
import robot.core.GameState
import robot.core.ecs.components.Car
import robot.core.ecs.components.Player
import robot.core.ecs.components.Remove
import robot.core.ecs.explosionAt

class PlayerCarDeathSystem: IteratingSystem(allOf(Car::class, Player::class).get()) {

    private var deathTimer = 1f
    private var needsExplosion = true

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if(Car.get(entity).health < 0f) {
            val body = Box2d.get(entity).body
            body.setLinearVelocity(0f, 0f)
            deathTimer -= deltaTime
            if(needsExplosion) {
                needsExplosion = false
                explosionAt(body.worldCenter, 1000f, 100f)
            }
            if(deathTimer < 0f) {
                GameState.playerDied()
                entity.addComponent<Remove>()
                deathTimer = 1f
                needsExplosion = true
            }
        }
    }
}

