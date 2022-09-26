package robot.core.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.ashley.mapperFor
import ktx.math.vec2

class Primitive: Component, Poolable {
    var primitive:Shape2D = Circle(vec2(), 5f)
    var color = Color.RED
    override fun reset() {
        primitive = Circle()
        color = Color.RED
    }

    companion object {
        val mapper = mapperFor<Primitive>()
        fun get(entity: Entity): Primitive {
            return mapper.get(entity)
        }

        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
    }
}