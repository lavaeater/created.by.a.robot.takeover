package robot.core.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class SpriteComponent: Component, Pool.Poolable {
    var texture = TextureRegion()
    override fun reset() {
        texture = TextureRegion()
    }
    companion object {
        val mapper = mapperFor<SpriteComponent>()
        fun get(entity: Entity): SpriteComponent {
            return mapper.get(entity)
        }
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
    }
}