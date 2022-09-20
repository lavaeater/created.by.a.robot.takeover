package robot.core

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.ExtendViewport
import eater.core.BasicScreen
import eater.core.engine
import eater.core.world
import eater.injection.InjectionContext
import eater.injection.InjectionContext.Companion.inject
import eater.input.CommandMap
import ktx.app.KtxGame
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.graphics.use
import ktx.math.vec2
import robot.core.ecs.createPlayerEntity
import robot.core.injection.Context
import space.earlygrey.shapedrawer.ShapeDrawer

class FirstScreen(val mainGame: KtxGame<KtxScreen>) : KtxScreen, KtxInputAdapter {
    init {
        Context.initialize()
    }

    val controlVector = vec2()
    val commandMap = CommandMap("Car Controls").apply {
        setBoth(Keys.W, "THROTTLE UP", { controlVector.y = 0f }, { controlVector.y = 1f} )
        setBoth(Keys.S, "HMM, REVERSE?", { controlVector.y = 0f }, { controlVector.y = -1f} )
        setBoth(Keys.A, "STEER LEFT", { controlVector.x = 0f }, { controlVector.x = -1f} )
        setBoth(Keys.D, "STEER RIGHT", { controlVector.x = 0f }, { controlVector.x = 1f} )
    }

    private val batch = inject<PolygonSpriteBatch>()

    //private val assets = inject<Assets>()
    private val viewPort: ExtendViewport by lazy { inject() }
    private val camera: OrthographicCamera by lazy { inject() }
    private val image = Texture("logo.png".toInternalFile(), true).apply {
        setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        )
    }
    private val timeStep = 1 / 60f
    private var accumulator = 0f
    private val velIters = 8
    private val posIters = 3

    private val shapeDrawerRegion: TextureRegion by lazy {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.drawPixel(0, 0)
        val texture = Texture(pixmap) //remember to dispose of later
        pixmap.disposeSafely()
        TextureRegion(texture, 0, 0, 1, 1)
    }

    val shapeDrawer by lazy {
        ShapeDrawer(inject<PolygonSpriteBatch>() as Batch, shapeDrawerRegion)
    }

    override fun show() {
        createPlayerEntity()
    }

    override fun resize(width: Int, height: Int) {
        viewPort.update(width, height)
        batch.projectionMatrix = camera.combined
    }

    override fun render(delta: Float) {
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)
        camera.update(false) //True or false, what's the difference?
        batch.projectionMatrix = camera.combined
        updatePhysics(delta)
        updateEngine(delta)
    }

    private fun updateEngine(delta: Float) {
        engine().update(delta)
    }

    private fun updatePhysics(delta: Float) {
        val ourTime = delta.coerceAtMost(timeStep * 2)
        accumulator += ourTime
        while (accumulator > timeStep) {
            world().step(timeStep, velIters, posIters)
            accumulator -= ourTime
        }
    }

    override fun dispose() {
        image.disposeSafely()
        batch.disposeSafely()
    }
}