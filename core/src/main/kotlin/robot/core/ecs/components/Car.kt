package robot.core.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.ashley.mapperFor

class Car : Component, Poolable {
    var maxForwardSpeed = 100000f
    var maxBackwardSpeed = 20000f
    var maxDriveForce = 150000f
    var controlState = 0
    override fun reset() {
        maxForwardSpeed = 100000f
        maxBackwardSpeed = 20000f
        maxDriveForce = 150000f
        controlState = 0
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