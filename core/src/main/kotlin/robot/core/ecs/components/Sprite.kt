package robot.core.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.math.vec2

class SpriteComponent: Component, Pool.Poolable {
    var tintColor: Color = Color.WHITE
    var shadowOffset = vec2(1f, -1f)
    var shadow = TextureRegion()
    var texture = TextureRegion()
    override fun reset() {
        tintColor = Color.WHITE
        shadowOffset = vec2(1f, -1f)
        texture = TextureRegion()
        shadow = TextureRegion()
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