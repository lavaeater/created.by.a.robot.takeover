package robot.core

import kotlin.experimental.or

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