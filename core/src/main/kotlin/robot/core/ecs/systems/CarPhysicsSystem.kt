package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import eater.ecs.components.Box2d
import ktx.ashley.allOf
import ktx.math.vec2
import robot.core.ecs.components.Car

fun Body.lateralVelocity(): Vector2 {
    val rightNormal = this.getWorldVector(vec2(1f, 0f))
    return rightNormal.scl(rightNormal.dot(linearVelocity))
}

fun Body.forwardVelocity(): Vector2 {
    val forward = this.getWorldVector(vec2(0f, 1f))
    return forward.scl(forward.dot(linearVelocity))
}

class CarPhysicsSystem:IteratingSystem(allOf(Car::class, Box2d::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        /**
         * Kill lateral velocity, as per
         * https://www.iforce2d.net/b2dtut/top-down-car
         */
        val body = Box2d.get(entity).body
        updateFriction(body)

    }

    fun updateFriction(body: Body) {
        val impulse = body.lateralVelocity().scl(-body.mass)
        body.applyLinearImpulse(impulse, body.worldCenter, true)
        body.applyAngularImpulse(body.inertia * 0.1f * -body.angularVelocity, true)

        val forward = body.forwardVelocity()
        val speed = forward.len()
        val dragForceMagnitude = -2f * speed
        body.applyForce(forward.scl(dragForceMagnitude), body.worldCenter, true)
    }
}