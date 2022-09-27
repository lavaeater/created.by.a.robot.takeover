package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import eater.ecs.components.Box2d
import eater.ecs.components.ExplosionComponent
import eater.injection.InjectionContext
import ktx.ashley.allOf
import ktx.graphics.use
import ktx.math.minus
import ktx.math.plus
import ktx.math.vec2
import space.earlygrey.shapedrawer.ShapeDrawer

class RenderExplosionSystem: IteratingSystem(allOf(ExplosionComponent::class, Box2d::class).get()) {
    private val shapeDrawer by lazy { InjectionContext.inject<ShapeDrawer>() }
    private val batch by lazy { InjectionContext.inject<PolygonSpriteBatch>() }

    override fun update(deltaTime: Float) {
        batch.use {
            super.update(deltaTime)
        }
    }
    val variationVectorRange = -20..20
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val position = Box2d.get(entity).body.worldCenter
        val ep = ExplosionComponent.get(entity)
        val lerpFactor = deltaTime / ep.explosionTime
        for(eb in ep.explosionBlorbs) {
            shapeDrawer.filledCircle(position + eb.position, eb.currentRadius, eb.backColor)
            shapeDrawer.filledCircle(position + eb.position + vec2(
                variationVectorRange.random() / 10f,
                variationVectorRange.random() / 10f
            ), eb.currentRadius * 0.5f, eb.frontColor)

            val ebDir = (position - eb.position).nor()
            eb.position.add(ebDir.scl(2f))

            eb.currentRadius += (eb.endRadius - eb.startRadius) * lerpFactor
        }
    }

}