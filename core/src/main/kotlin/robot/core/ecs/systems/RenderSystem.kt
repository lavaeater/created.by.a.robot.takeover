package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.MathUtils.radiansToDegrees
import eater.ecs.components.Box2d
import ktx.ashley.allOf
import ktx.graphics.use
import robot.core.ecs.components.SpriteComponent

class RenderSystem(private val batch: PolygonSpriteBatch) :
    IteratingSystem(allOf(Box2d::class, SpriteComponent::class).get()) {
    override fun update(deltaTime: Float) {
        batch.use { super.update(deltaTime) }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val region = SpriteComponent.get(entity).texture
        val body = Box2d.get(entity).body
        val position = body.worldCenter
        batch.draw(
            region,
            position.x - region.regionWidth / 2,
            position.y - region.regionHeight / 2,
            region.regionWidth / 2f,
            region.regionHeight / 2f,
            region.regionWidth.toFloat(),
            region.regionHeight.toFloat(),
            1f,
            1f,
            body.angle * radiansToDegrees
        )
    }
}