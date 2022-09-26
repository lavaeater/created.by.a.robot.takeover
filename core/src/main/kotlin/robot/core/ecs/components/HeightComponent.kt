package robot.core.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class HeightComponent: Component, Pool.Poolable {
    var height = 0f
    var flightSpeed = 20f
    var maxHeight = 5f
    override fun reset() {
        flightSpeed = 20f
        height = 0f
        maxHeight = 5f
    }
    companion object {
        val mapper = mapperFor<HeightComponent>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }

        fun get(entity:Entity): HeightComponent {
            return mapper.get(entity)
        }
    }
}