package robot.core.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool.Poolable
import com.badlogic.gdx.utils.Queue
import ktx.ashley.mapperFor
import robot.core.ecs.PickupType



class Car : Component, Poolable {
    var lastPickup: PickupType? = null
    var canRace = true
    val weapons = Queue<PickupType>()
    var health = 100f
    var maxTorque = 300f
    var maxForwardSpeed = 10000f
    var maxBackwardSpeed = 2000f
    var maxDriveForce = 10000f
    var currentDriveForce = 0f
    var acceleration = 100f
    var decceleration = 500f
    var controlState = 0
    var immortalTimer = 1f

    var immortalMax = 1f
    val immortal get() = immortalTimer > 0f
    val mortal get() = !immortal
    var immortalAdder = 1f
    fun addToImmortalTimer(t: Float) {
        if(immortalTimer < 0f)
            immortalTimer = 0f
        immortalTimer += t
        immortalMax = immortalTimer
    }
    override fun reset() {
        immortalTimer = 1f
        canRace = true
        weapons.clear()
        health = 100f
        maxForwardSpeed = 10000f
        maxBackwardSpeed = 2000f
        maxDriveForce = 10000f
        maxTorque = 100f
        controlState = 0
        currentDriveForce = 0f
        acceleration = 10f
    }

    fun takeDamage(damage:Float) {
        if(mortal) {
            health -= damage
            addToImmortalTimer(immortalAdder)
        }
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