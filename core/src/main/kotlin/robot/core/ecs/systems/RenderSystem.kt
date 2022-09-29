package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.MathUtils.radiansToDegrees
import eater.ecs.components.Box2d
import eater.extensions.draw
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import ktx.graphics.use
import ktx.math.plus
import ktx.math.times
import ktx.math.vec2
import robot.core.GameConstants.MetersPerPixel
import robot.core.ecs.components.Car
import robot.core.ecs.components.HeightComponent
import robot.core.ecs.components.SpriteComponent
import robot.core.track.TrackMania
import space.earlygrey.shapedrawer.ShapeDrawer

class RenderSystem(private val batch: PolygonSpriteBatch) :
    IteratingSystem(allOf(Box2d::class, SpriteComponent::class).get()) {

    val trackMania by lazy { inject<TrackMania>() }
    val shapeDrawer by lazy { inject<ShapeDrawer>() }

    override fun update(deltaTime: Float) {
        batch.use {
            renderTrack()
            super.update(deltaTime)
        }
    }

    val odd = Color(0.21f, 0.21f, 0.21f, 1f)
    val oddLine = Color(0.18f, 0.18f, 0.18f, 1f)
    val evenLine = Color(0.23f, 0.23f, 0.23f, 1f)
    val even = Color(0.2f, 0.2f, 0.2f, 1f)
    val lineColor = Color(0.7f, 0.7f, 0.7f, 1f)
    private fun renderTrack() {

        for ((i, p) in trackMania.polygons.withIndex()) {
            if (i % 2 == 0)
                shapeDrawer.setColor(even)
            else
                shapeDrawer.setColor(odd)

            shapeDrawer.filledPolygon(p)
        }

        for ((i, center) in trackMania.track.map { it.center }.withIndex()) {
            if (i % 2 == 0) {
                shapeDrawer.setColor(lineColor)
                if (i < trackMania.track.lastIndex)
                    shapeDrawer.line(center, trackMania.track[i + 1].center)
            }

        }

        for ((i, left) in trackMania.track.map { it.left }.withIndex()) {
            if (i % 2 == 0)
                shapeDrawer.setColor(oddLine)
            else
                shapeDrawer.setColor(evenLine)
            if (i < trackMania.track.lastIndex)
                shapeDrawer.line(left, trackMania.track[i + 1].left)
        }
        for ((i, right) in trackMania.track.map { it.right }.withIndex()) {
            if (i % 2 == 0)
                shapeDrawer.setColor(oddLine)
            else
                shapeDrawer.setColor(evenLine)
            if (i < trackMania.track.lastIndex)
                shapeDrawer.line(right, trackMania.track[i + 1].right)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val sprite = SpriteComponent.get(entity)
        val region = sprite.texture
        val shadow = sprite.shadow
        val body = Box2d.get(entity).body
        val position = body.worldCenter
        if(Car.has(entity)) {
            val car = Car.get(entity)
            if(car.immortal) {
                val alpha = MathUtils.norm(0f, car.immortalMax, car.immortalTimer)
                shapeDrawer.filledCircle(position, 4f, Color(0.5f, 0.5f, 1f, alpha))
            }
        }


        if (HeightComponent.has(entity)) {
            val height = HeightComponent.get(entity)
            val scale = 1f + height.height / height.maxHeight
            val sPos = vec2(position.x + height.height, position.y)
            shadow.draw(
                batch,
                sPos + vec2(1f * MetersPerPixel, -1f * MetersPerPixel),
                body.angle * radiansToDegrees,
                MetersPerPixel
            )
            batch.setColor(sprite.tintColor)
            region.draw(batch, position, body.angle * radiansToDegrees, MetersPerPixel * scale)
            batch.setColor(Color.WHITE)
        } else {
            shadow.draw(
                batch,
                position + sprite.shadowOffset * MetersPerPixel,
                body.angle * radiansToDegrees,
                MetersPerPixel
            )
            batch.setColor(sprite.tintColor)
            region.draw(batch, position, body.angle * radiansToDegrees, MetersPerPixel)
            batch.setColor(Color.WHITE)
        }
    }
}