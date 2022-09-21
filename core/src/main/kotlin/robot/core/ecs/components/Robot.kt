package robot.core.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class Robot : Component, Pool.Poolable {
    override fun reset() {
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