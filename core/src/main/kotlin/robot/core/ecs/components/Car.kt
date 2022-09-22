package robot.core.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.ashley.mapperFor

class Car : Component, Poolable {
    var maxTorque = 300f
    var maxForwardSpeed = 10000f
    var maxBackwardSpeed = 2000f
    var maxDriveForce = 10000f
    var currentDriveForce = 0f
    var acceleration = 100f
    var decceleration = 500f
    var controlState = 0
    override fun reset() {
        maxForwardSpeed = 10000f
        maxBackwardSpeed = 2000f
        maxDriveForce = 10000f
        maxTorque = 100f
        controlState = 0
        currentDriveForce = 0f
        acceleration = 10f
    }

    companion object {
        val mapper = mapperFor<Car>()
        fun get(entity: Entity): Car {
            return mapper.get(entity)
        }
        fun has(entity: Entity):Boolean {
            return mapper.has(entity)
        }
        val forward = 1
        val backwards = 2
        val left = 4
        val right = 8
    }
}