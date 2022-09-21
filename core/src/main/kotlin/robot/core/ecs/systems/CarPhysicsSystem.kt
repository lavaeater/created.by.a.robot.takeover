package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import eater.ecs.components.Box2d
import eater.physics.forwardNormal
import eater.physics.forwardVelocity
import eater.physics.lateralVelocity
import ktx.ashley.allOf
import ktx.math.vec2
import robot.core.ecs.components.Car
import robot.core.has

class CarPhysicsSystem : IteratingSystem(allOf(Car::class, Box2d::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        /**
         * Kill lateral velocity, as per
         * https://www.iforce2d.net/b2dtut/top-down-car
         */
        val body = Box2d.get(entity).body
        updateDrive(body, Car.get(entity))
        this.updateFriction(body)
    }

    private fun updateDrive(body: Body, car: Car) {
        var desiredSpeed = 0f
        if (car.controlState.has(Car.forward))
            desiredSpeed = car.maxForwardSpeed
        else if (car.controlState.has(Car.backwards))
            desiredSpeed = car.maxBackwardSpeed

        val forwardVelocity = body.forwardVelocity()
        val forwardNormal = body.forwardNormal()
        val currentSpeed = body.forwardVelocity().dot(forwardNormal)
        var force = 0f
        if (desiredSpeed > currentSpeed)
            force = car.maxDriveForce
        else if (desiredSpeed < currentSpeed)
            force = -car.maxDriveForce

        body.applyForce(forwardNormal.scl(force), body.worldCenter, true)

        var desiredTorque = 0f
        if (car.controlState.has(Car.left))
            desiredTorque = 150f
        else if (car.controlState.has(Car.right))
            desiredTorque = -150f
        body.applyTorque(desiredTorque, true)
    }

    private val maxLateralImpulse = 3f
    private fun updateFriction(body: Body) {
        val impulse = body.lateralVelocity().scl(-body.mass)
        if (impulse.len() > maxLateralImpulse)
            impulse.scl(maxLateralImpulse / impulse.len())
        body.applyLinearImpulse(impulse, body.worldCenter, true)



        body.applyAngularImpulse(body.inertia * 0.1f * -body.angularVelocity, true)

        val forward = body.forwardVelocity()
        val speed = forward.len()
        val dragForceMagnitude = -2f * speed
        body.applyForce(forward.scl(dragForceMagnitude), body.worldCenter, true)
    }
}