package robot.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.ExtendViewport
import eater.core.engine
import eater.core.world
import eater.injection.InjectionContext.Companion.inject
import eater.input.CommandMap
import eater.input.KeyPress
import ktx.app.KtxGame
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.*
import ktx.graphics.use
import ktx.math.random
import ktx.math.vec2
import robot.core.ecs.components.Car
import robot.core.ecs.createPlayerEntity
import robot.core.injection.Context
import space.earlygrey.shapedrawer.ShapeDrawer

fun Int.has(flag: Int) = flag and this == flag
fun Int.with(flag: Int) = this or flag
fun Int.without(flag: Int) = this and flag.inv()

object Assets: DisposableRegistry by DisposableContainer() {
    private val blueCarTexture = Texture("cars/player-blue.png".toLocalFile(),true).apply {
//        setFilter(
//            Texture.TextureFilter.Linear,
//            Texture.TextureFilter.Linear
//        )
    }.alsoRegister()
    val blueCarRegion by lazy {
        Array(8) {
            TextureRegion(blueCarTexture, it * 16, 0, 16, 16)
        }
    }

}

class FirstScreen(val mainGame: KtxGame<KtxScreen>) : KtxScreen, KtxInputAdapter {
    init {
        Context.initialize()
    }

    val randomRange = (-500f..500f)
    val cloudOfDots = Array(1000) {
        vec2(randomRange.random(), randomRange.random())
    }
    val playerEntity by lazy { createPlayerEntity(2f, 4f,) }
    val playerCar by lazy { Car.get(playerEntity) }
    val commandMap = CommandMap("Car Controls").apply {
        setBoth(Keys.W, "THROTTLE UP", { removeFlag(Car.forward) }, { addFlag(Car.forward) })
        setBoth(Keys.S, "HMM, REVERSE?", { removeFlag(Car.backwards) }, { addFlag(Car.backwards) })
        setBoth(Keys.A, "STEER LEFT", { removeFlag(Car.left) }, { addFlag(Car.left) })
        setBoth(Keys.D, "STEER RIGHT", { removeFlag(Car.right) }, { addFlag(Car.right) })
    }

    private fun addFlag(flag: Int) {
        playerCar.controlState = playerCar.controlState.with(flag)
    }

    private fun removeFlag(flag: Int) {
        playerCar.controlState = playerCar.controlState.without(flag)
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
        Gdx.input.inputProcessor = this
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun resize(width: Int, height: Int) {
        viewPort.update(width, height)
        batch.projectionMatrix = camera.combined
    }


    override fun keyDown(keycode: Int): Boolean {
        return commandMap.execute(keycode, KeyPress.Down)
    }

    override fun keyUp(keycode: Int): Boolean {
        return commandMap.execute(keycode, KeyPress.Up)
    }

    override fun render(delta: Float) {
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)
        camera.update(false) //True or false, what's the difference?
        batch.projectionMatrix = camera.combined
        updatePhysics(delta)
        updateEngine(delta)
        batch.use {
            for(v in cloudOfDots)
                shapeDrawer.filledCircle(v, 2.5f, Color.GREEN)
        }
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
        Assets.disposeSafely()
    }
}