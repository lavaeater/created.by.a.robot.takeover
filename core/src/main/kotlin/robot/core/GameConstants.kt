package robot.core

import kotlin.experimental.and
import kotlin.experimental.or

object GameConstants {
    const val GameWidth = 48f
    const val GameHeight = (16f/9f) * GameWidth
    const val PixelsPerMeter = 4f
    const val MetersPerPixel = 1f / PixelsPerMeter
    const val DragForceMagnitudeFactor = -0.1f
    const val WindDragForceMagnitudeFactor = -0.05f
    const val MaxLateralImpulse = 5f

    const val TimeStep = 1 / 60f
    const val VelIters = 16
    const val PosIters = 6

    const val MinRobots = 15
    const val RobotMaxDistance = 500f
    const val RobotMaxOfPlayerSpeed = 1.25f
    const val RobotMinOfPlayerSpeed = 0.5f
}

object Box2dCategories {
    const val none: Short = 0
    const val cars: Short = 1
    const val terrain: Short = 2
    const val projectiles: Short = 4
    const val pickups: Short = 8
    const val explosions: Short = 16
    const val sensors: Short = 32

    val carsCollideWith = cars or terrain or projectiles or pickups or explosions or sensors
    val terrainCollidesWith = cars or projectiles or sensors
    val projectilesCollideWith = cars or terrain or sensors
    val pickupsCollideWith = cars
    val explosionsCollideWith = cars
}