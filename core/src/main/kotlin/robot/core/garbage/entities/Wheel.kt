package robot.core.garbage.entities

import com.badlogic.gdx.physics.box2d.World
import com.topdowncar.game.BodyHolder

class Wheel
/**
 * Base constructor wor Wheel
 * @param position wheel position
 * @param size wheel size
 * @param world [com.topdowncar.game.screens.PlayScreen.mWorld] used to control and add physics objects
 * @param id wheel unique IS
 * @param car [Car] class used to set correct wheel angle depending of car rotation
 * @param powered is wheel powered or not
 */(
    position: Vector2?, size: Vector2?, world: World?, id: Int, private val mCar: Car,
    /**
     * Returning boolean value if wheel is powered or not
     * @return powered wheel or not
     */
    val isPowered: Boolean
) : BodyHolder(position, size, BodyDef.BodyType.DynamicBody, world, WHEEL_DENSITY, true, id) {
    /**
     * Set wheel angle
     * @param angle angle to which to rotate the wheel
     */
    fun setAngle(angle: Float) {
        getBody().setTransform(getBody().getPosition(), mCar.getBody().getAngle() + angle * DEGTORAD)
    }

    companion object {
        const val UPPER_LEFT = 0
        const val UPPER_RIGHT = 1
        const val DOWN_LEFT = 2
        const val DOWN_RIGHT = 3
        private const val WHEEL_DENSITY = 0.4f
        private const val DEGTORAD = 0.0174532925199432957f
    }
}