package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.MathUtils.radiansToDegrees
import com.badlogic.gdx.math.Vector2
import eater.ecs.components.Box2d
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import ktx.graphics.use
import ktx.math.plus
import ktx.math.vec2
import robot.core.GameConstants.MetersPerPixel
import robot.core.ecs.components.SpriteComponent
import robot.core.track.TrackMania
import space.earlygrey.shapedrawer.ShapeDrawer

fun TextureRegion.draw(batch: PolygonSpriteBatch, position: Vector2, rotation:Float, scale: Float) {
    batch.draw(
        this,
        position.x - this.regionWidth / 2f,
        position.y - this.regionHeight / 2f,
        this.regionWidth / 2f,
        this.regionHeight / 2f,
        this.regionWidth.toFloat(),
        this.regionHeight.toFloat(),
        scale,
        scale,
        rotation
    )
}

class RenderSystem(private val batch: PolygonSpriteBatch) :
    IteratingSystem(allOf(Box2d::class, SpriteComponent::class).get()) {

    val trackMania = TrackMania()
    val points = trackMania.buildTrack()
    val widths = trackMania.setWidths(100)
    val shapeDrawer by lazy { inject<ShapeDrawer>() }

    override fun update(deltaTime: Float) {
        batch.use {

        renderTrack()
            super.update(deltaTime)
        }
    }

    private fun renderTrack() {
        shapeDrawer.setColor(Color.DARK_GRAY)
        for(i in 0..points.lastIndex) {
            if(i > 0) {
                shapeDrawer.line(points[i - 1], points[i], widths[i-1])
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val sprite = SpriteComponent.get(entity)
        val region = sprite.texture
        val shadow = sprite.shadow
        val body = Box2d.get(entity).body
        val position = body.worldCenter
        shadow.draw(batch, position + vec2(1f * MetersPerPixel,-1f * MetersPerPixel), body.angle * radiansToDegrees, MetersPerPixel)
        region.draw(batch, position, body.angle * radiansToDegrees, MetersPerPixel)
    }
}