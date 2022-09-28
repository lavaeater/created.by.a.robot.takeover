package robot.core.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.math.vec2

class Robot : Component, Pool.Poolable {
    val target = vec2()
    var targetIndex = 0
    var shotTimer = 0f
    override fun reset() {
        target.setZero()
    }

    companion object {
        val mapper = mapperFor<Robot>()
        fun get(entity: Entity): Robot {
            return mapper.get(entity)
        }

        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
    }
}