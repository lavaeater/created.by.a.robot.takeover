package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils.radiansToDegrees
import com.badlogic.gdx.math.Vector2
import eater.ecs.components.Box2d
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import ktx.graphics.use
import ktx.math.plus
import ktx.math.vec2
import robot.core.GameConstants.MetersPerPixel
import robot.core.ecs.components.HeightComponent
import robot.core.ecs.components.SpriteComponent
import robot.core.track.TrackMania
import space.earlygrey.shapedrawer.ShapeDrawer

fun TextureRegion.draw(batch: PolygonSpriteBatch, position: Vector2, rotation: Float, scale: Float) {
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

    val trackMania by lazy { inject<TrackMania>() }
    val track get() = trackMania.track
    val shapeDrawer by lazy { inject<ShapeDrawer>() }

    override fun update(deltaTime: Float) {
        batch.use {
            renderTrack()
            super.update(deltaTime)
        }
    }

    private fun renderTrack() {
        shapeDrawer.setColor(Color.DARK_GRAY)
        for((index, section) in track.withIndex()) {
            if(index < track.lastIndex) {
                shapeDrawer.line(section.left, track[index + 1].left, .25f)
                shapeDrawer.line(section.center, track[index + 1].center, .25f)
                shapeDrawer.line(section.right, track[index + 1].right, .25f)
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val sprite = SpriteComponent.get(entity)
        val region = sprite.texture
        val shadow = sprite.shadow
        val body = Box2d.get(entity).body
        val position = body.worldCenter
        if(HeightComponent.has(entity)) {
            val height = HeightComponent.get(entity)
            val scale = 1f + height.height / height.maxHeight
            val sPos = vec2(position.x + height.height, position.y)
            shadow.draw(
                batch,
                sPos + vec2(1f * MetersPerPixel, -1f * MetersPerPixel),
                body.angle * radiansToDegrees,
                MetersPerPixel
            )
            region.draw(batch, position, body.angle * radiansToDegrees, MetersPerPixel * scale)
        } else {
            shadow.draw(
                batch,
                position + vec2(1f * MetersPerPixel, -1f * MetersPerPixel),
                body.angle * radiansToDegrees,
                MetersPerPixel
            )
            region.draw(batch, position, body.angle * radiansToDegrees, MetersPerPixel)
        }
    }
}