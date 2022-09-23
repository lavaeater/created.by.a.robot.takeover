package robot.core.injection

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.ExtendViewport
import eater.ecs.systems.CameraUpdateSystem
import eater.ecs.systems.PhysicsDebugRendererSystem
import eater.ecs.systems.UtilityAiSystem
import eater.injection.InjectionContext
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import robot.core.GameConstants.GameHeight
import robot.core.GameConstants.GameWidth
import robot.core.ecs.systems.CarPhysicsSystem
import robot.core.ecs.systems.RenderSystem
import robot.core.track.TrackMania
import space.earlygrey.shapedrawer.ShapeDrawer


object Context : InjectionContext() {
    private val shapeDrawerRegion: TextureRegion by lazy {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.drawPixel(0, 0)
        val texture = Texture(pixmap) //remember to dispose of later
        pixmap.disposeSafely()
        TextureRegion(texture, 0, 0, 1, 1)
    }

    fun initialize() {
        buildContext {
            bindSingleton(PolygonSpriteBatch())
            bindSingleton(OrthographicCamera())
            bindSingleton(
                ExtendViewport(
                    GameWidth,
                    GameHeight,
                    inject<OrthographicCamera>() as Camera
                )
            )
            bindSingleton(createWorld())
            bindSingleton(getEngine())
            bindSingleton(ShapeDrawer(inject<PolygonSpriteBatch>() as Batch, shapeDrawerRegion))
            bindSingleton(TrackMania().apply {
                this.track.addAll(this.getTrack(1000, 10, 150f..350f, -5..5))
            })
//            bindSingleton(Hud(inject()))
        }
    }

    private fun getEngine(): Engine {
        return PooledEngine().apply {
            addSystem(CameraUpdateSystem(inject(), inject()))
            addSystem(RenderSystem(inject()))
            addSystem(PhysicsDebugRendererSystem(inject(), inject()))
            addSystem(CarPhysicsSystem())
            addSystem(UtilityAiSystem())
//            addSystem(UpdateActionsSystem())
        }
    }
}