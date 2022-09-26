package robot.core.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.math.vec2

class GuidedMissile:Component, Pool.Poolable {
    var baseSpeed = 0f
    var startDirection = vec2()
    var damage = 100f
    var radius = 25f
    var flightTime = 2.5f
    var armTime = flightTime * 0.9f
    val armed get() = flightTime < armTime
    var maxSpeed = 10000f
    var force = 1000f
    var hasTarget = false
    var target: Body? = null
    var torque = 5f
    override fun reset() {
        baseSpeed = 0f
        startDirection = vec2()
        flightTime = 2.5f
        armTime = flightTime * 0.9f
        force = 1000f
        hasTarget = false
        target = null
        maxSpeed = 10000f
        torque = 500f
    }

    companion object {
        val mapper = mapperFor<GuidedMissile>()
        fun get(entity: Entity): GuidedMissile {
            return mapper.get(entity)
        }
        fun has(entity: Entity):Boolean {
            return mapper.has(entity)
        }
    }
}