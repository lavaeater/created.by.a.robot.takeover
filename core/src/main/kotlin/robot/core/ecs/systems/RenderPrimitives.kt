package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Ellipse
import com.badlogic.gdx.math.MathUtils.radiansToDegrees
import com.badlogic.gdx.math.Rectangle
import eater.ecs.components.Box2d
import eater.injection.InjectionContext
import ktx.ashley.allOf
import ktx.graphics.use
import robot.core.ecs.components.Primitive
import space.earlygrey.shapedrawer.ShapeDrawer

class RenderPrimitives(private val batch: PolygonSpriteBatch) :
    IteratingSystem(allOf(Box2d::class, Primitive::class).get()) {
    val shapeDrawer by lazy { InjectionContext.inject<ShapeDrawer>() }

    override fun update(deltaTime: Float) {
        batch.use {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val p = Primitive.get(entity)
        val body = Box2d.get(entity).body
        when (p.primitive) {
            is Circle -> shapeDrawer.filledCircle(body.position, (p.primitive as Circle).radius, p.color)
            is Rectangle -> {
                val r = p.primitive as Rectangle
                r.x = body.position.x - r.width / 2
                r.y = body.position.y - r.height / 2
                shapeDrawer.filledRectangle(r, p.color)
            }

            is Ellipse -> {
                val e = p.primitive as Ellipse
                shapeDrawer.filledEllipse(
                    body.position.x,
                    body.position.y,
                    e.height,
                    e.width,
                    body.angle * radiansToDegrees,
                    p.color,
                    p.color)
            }

            else -> {
//No-op
            }
        }
    }
}