package robot.core.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.utils.viewport.ExtendViewport
import eater.core.engine
import eater.core.world
import eater.ecs.components.Box2d
import eater.injection.InjectionContext.Companion.inject
import eater.input.CommandMap
import eater.input.KeyPress
import eater.physics.forwardNormal
import eater.physics.forwardVelocity
import ktx.app.KtxGame
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.math.plus
import ktx.math.vec2
import robot.core.Assets
import robot.core.GameConstants.PosIters
import robot.core.GameConstants.TimeStep
import robot.core.GameConstants.VelIters
import robot.core.RoboGame
import robot.core.ecs.components.Car
import robot.core.ecs.createPlayerEntity
import robot.core.ecs.createRobotCar
import robot.core.ecs.fireProjectile
import robot.core.injection.Context
import robot.core.ui.Hud
import robot.core.with
import robot.core.without
import space.earlygrey.shapedrawer.ShapeDrawer

class GameScreen(private val game: RoboGame) : KtxScreen, KtxInputAdapter {
    val randomRange = (-500f..500f)
    val playerEntity by lazy { createPlayerEntity(vec2(),2f, 4f) }
//    val robots by lazy {
//        Array(100) {
//            val x = -50f + it
//            createRobotCar(vec2(x,-100f),2f,4f)
//        }
//    }
    val playerCar by lazy { Car.get(playerEntity) }
    val commandMap = CommandMap("Car Controls").apply {
        setBoth(Keys.W, "THROTTLE UP", { removeFlag(Car.forward) }, { addFlag(Car.forward) })
        setBoth(Keys.S, "HMM, REVERSE?", { removeFlag(Car.backwards) }, { addFlag(Car.backwards) })
        setBoth(Keys.A, "STEER LEFT", { removeFlag(Car.left) }, { addFlag(Car.left) })
        setBoth(Keys.D, "STEER RIGHT", { removeFlag(Car.right) }, { addFlag(Car.right) })
        setDown(Keys.SPACE, "FIRE") { fire() }
    }

    private fun fire() {
        if(playerCar.weapons.any()) {
            val weaponToFire = playerCar.weapons.removeFirst()
            val playerBody = Box2d.get(playerEntity).body
            val forwardNormal = playerBody.forwardNormal()

            val forwardSpeed = playerBody.forwardVelocity().dot(forwardNormal)
            fireProjectile(playerBody.worldCenter + forwardNormal.cpy().scl(2f), forwardNormal, forwardSpeed, weaponToFire)
        }
    }

    private val hud by lazy { inject<Hud>() }

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
    private val shapeDrawer: ShapeDrawer by lazy { inject() }
    private var accumulator = 0f

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
        hud.render(delta)
    }

    private fun updateEngine(delta: Float) {
        engine().update(delta)
    }

    private fun updatePhysics(delta: Float) {
        val ourTime = delta.coerceAtMost(TimeStep * 2)
        accumulator += ourTime
        while (accumulator > TimeStep) {
            world().step(TimeStep, VelIters, PosIters)
            accumulator -= ourTime
        }
    }

    override fun dispose() {
        image.disposeSafely()
        batch.disposeSafely()
        Assets.disposeSafely()
    }
}