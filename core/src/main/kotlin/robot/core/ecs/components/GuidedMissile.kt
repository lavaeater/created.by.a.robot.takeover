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
    var flightTime = 5f
    var armTime = 4.75f
    val armed get() = flightTime < armTime
    var maxSpeed = 100f
    var force = 10f
    var hasTarget = false
    var target: Body? = null
    var torque = 50f
    override fun reset() {
        baseSpeed = 0f
        startDirection = vec2()
        armTime = 4.5f
        force = 10f
        hasTarget = false
        target = null
        flightTime = 5f
        maxSpeed = 100f
        torque = 50f
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