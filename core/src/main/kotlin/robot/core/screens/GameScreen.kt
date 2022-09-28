package robot.core.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
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
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.ashley.allOf
import ktx.ashley.remove
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.graphics.use
import ktx.math.plus
import ktx.math.times
import ktx.math.vec2
import robot.core.*
import robot.core.GameConstants.MinRobots
import robot.core.GameConstants.PosIters
import robot.core.GameConstants.TimeStep
import robot.core.GameConstants.VelIters
import robot.core.ecs.components.Car
import robot.core.ecs.components.Robot
import robot.core.ecs.createPlayerEntity
import robot.core.ecs.createRobotCar
import robot.core.ecs.explosionAt
import robot.core.ecs.fireProjectile
import robot.core.track.TrackMania
import robot.core.ui.Hud
import space.earlygrey.shapedrawer.ShapeDrawer
import kotlin.math.roundToInt

class GameScreen(private val game: RoboGame) : KtxScreen, KtxInputAdapter {
    val randomRange = (-500f..500f)

    val playerCar get() = Car.get(GameState.playerEntity)
    val commandMap = CommandMap("Car Controls").apply {
        setBoth(Keys.W, "THROTTLE UP", { removeFlag(Car.forward) }, { addFlag(Car.forward) })
        setBoth(Keys.S, "HMM, REVERSE?", { removeFlag(Car.backwards) }, { addFlag(Car.backwards) })
        setBoth(Keys.A, "STEER LEFT", { removeFlag(Car.left) }, { addFlag(Car.left) })
        setBoth(Keys.D, "STEER RIGHT", { removeFlag(Car.right) }, { addFlag(Car.right) })
        setBoth(Keys.SPACE, "FIRE", { stopFiring() }) { startFiring() }
    }

    private val trackMania by lazy { inject<TrackMania>() }

    private var firing = false
    private fun startFiring() {
        firing = true
    }

    private fun stopFiring() {
        firing = false
    }

    private fun fire() {
        if (firing) {
            if (playerCar.currentWeapon == null && playerCar.weapons.any()) {
                playerCar.currentWeapon = playerCar.weapons.removeFirst()
                playerCar.currentAmmo = playerCar.currentWeapon!!.ammo
            }
            if (playerCar.currentWeapon != null) {
                val weaponToFire = playerCar.currentWeapon!!
                if (playerCar.currentAmmo > 0 && shotTimer <= 0f) {
                    playerCar.currentAmmo--
                    if (playerCar.currentAmmo <= 0)
                        playerCar.currentWeapon = null

                    val playerBody = Box2d.get(GameState.playerEntity).body
                    val forwardNormal = playerBody.forwardNormal()

                    val forwardSpeed = playerBody.forwardVelocity().dot(forwardNormal)
                    fireProjectile(
                        playerBody.worldCenter + forwardNormal.cpy().scl(2f),
                        forwardNormal,
                        forwardSpeed,
                        weaponToFire,
                        true
                    )
                    shotTimer = 1f / weaponToFire.rof
                }
            }
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
        drawStartBlorbs = true
        Gdx.input.inputProcessor = this
        trackMania.createTrack(50, 10, 5..15, -5..5)

        GameState.playerEntity = createPlayerEntity(vec2(), 2f, 4f)
        createStartRobots(MinRobots / 2)
        for (carEntity in engine().getEntitiesFor(allOf(Car::class).get())) {
            Car.get(carEntity).canRace = false
        }
        for (system in engine().systems) {
            system.setProcessing(true)
        }
    }

    private fun createStartRobots(numberOfRobots: Int) {
        val startSection = trackMania.track.first()

        for (i in 1..numberOfRobots) {
            val factor = if (i % 2 == 0) 1f else -1f
            createRobotCar(startSection.center + vec2(4f * i * factor, 0f), 2f, 4f)
        }
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
        for (entity in engine().getEntitiesFor(allOf(Box2d::class).get())) {
            val body = Box2d.get(entity).body
            entity.remove<Box2d>()
            world().destroyBody(body)
        }
        trackMania.clearTrack()
        engine().removeAllEntities()
        for (system in engine().systems) {
            system.setProcessing(false)
        }
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

    private var shotTimer = 0f

    override fun render(delta: Float) {
        clearScreen(red = 0.15f, green = 0.15f, blue = 0.15f)
        camera.update(false) //True or false, what's the difference?
        batch.projectionMatrix = camera.combined
        updatePhysics(delta)
        updateEngine(delta)
        hud.render(delta)
        checkGameOver()
        checkRaceStart(delta)
        renderMiniMap()
        if (shotTimer > 0f)
            shotTimer -= delta
        fire()
    }

    private val offsetVector = vec2(50f, 50f)
    private val robots = allOf(Robot::class).get()
    private val robotPositions get() = engine().getEntitiesFor(robots).map { Box2d.get(it).body.worldCenter }

    private fun renderMiniMap() {
        shapeDrawer.setColor(Color.GREEN)
        batch.use {
            for ((i, center) in trackMania.track.map { it.center }.withIndex()) {
                if (i < trackMania.track.lastIndex)
                    shapeDrawer.line(
                        offsetVector + center * 0.015f,
                        offsetVector + trackMania.track[i + 1].center * 0.015f
                    )
            }
            if (GameState.playerReady) {

                for (robot in robotPositions)
                    shapeDrawer.filledCircle(
                        offsetVector + robot * 0.015f,
                        0.5f,
                        Color.RED
                    )

                shapeDrawer.filledCircle(
                    offsetVector + Box2d.get(GameState.playerEntity).body.worldCenter * 0.015f,
                    0.5f,
                    Color.WHITE
                )
            }
        }
    }

    private var drawStartBlorbs = true

    private fun checkRaceStart(delta: Float) {
        if (GameState.startCountDown > 0f) {
            GameState.startCountDown -= delta
            drawStartBlorbs()
            if (GameState.startCountDown < 0f) {
                drawStartBlorbs = false
                for (carEntity in engine().getEntitiesFor(allOf(Car::class).get())) {
                    Car.get(carEntity).canRace = true
                }
                GameState.raceStarted = true
            }
        }
    }

    private val redBlorb = Color.RED
    private val yellowBlorb = Color.YELLOW
    private val greenBlorb = Color.GREEN
    fun drawStartBlorbs() {
        if (drawStartBlorbs) {
            val countDown = GameState.startCountDown.roundToInt()
            val x = when (countDown) {
                3 -> 150f
                2 -> 150f
                1 -> 200f
                0 -> 250f
                else -> 150f
            }
            val blorbPos = vec2(x, 200f)
            val color = when (countDown) {
                3 -> redBlorb
                2 -> redBlorb
                1 -> yellowBlorb
                0 -> greenBlorb
                else -> redBlorb
            }
            batch.use {
                shapeDrawer.filledCircle(blorbPos, 25f, color)
            }
        }
    }

    private fun checkGameOver() {
        if (GameState.gameStarted) {
            if (GameState.playerDied)
                game.playerDied()
            else if (GameState.playerWon)
                game.playerWon()
        }
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
        if (GameState.explosionQueue.any()) {
            val ed = GameState.explosionQueue.removeFirst()
            explosionAt(ed.position, ed.damage, ed.radius)
        }
    }

    override fun dispose() {
        image.disposeSafely()
        batch.disposeSafely()
        Assets.disposeSafely()
    }
}