package robot.core.garbage

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import robot.core.garbage.tools.ShapeFactory

abstract class BodyHolder {
    private var mForwardSpeed: Vector2? = null
    private var mLateralSpeed: Vector2? = null
    private val mBody: Body
    private var mDrift = 1f
    private val mId: Int

    /**
     * Most base constructor used if we already have a body that we need to control by the logic
     * in this class
     * @param mBody body we have already created
     */
    constructor(mBody: Body) {
        this.mBody = mBody
        mId = -1
    }

    /**
     * Advanced constructor where we need to pass in all needed information in order to create body
     * @param position rectangle position
     * @param size rectangle size
     * @param type rectangle type
     * @param world [com.topdowncar.game.screens.PlayScreen.mWorld] used to control and add physics objects
     * @param density rectangle density
     * @param sensor is fixture a sensor
     * @param id unique ID
     */
    constructor(
        position: Vector2,
        size: Vector2,
        type: BodyDef.BodyType,
        world: World,
        density: Float,
        sensor: Boolean,
        id: Int
    ) {
        mBody = ShapeFactory.createRectangle(position, size, type, world, density, sensor)
        mId = id
    }

    /**
     * Main logic update method
     * @param delta delta time received from [com.topdowncar.game.screens.PlayScreen.render]
     */
    open fun update(delta: Float) {
        if (mDrift < 1) {
            mForwardSpeed = forwardVelocity
            mLateralSpeed = lateralVelocity
            if (mLateralSpeed?.len()!! < DRIFT_OFFSET && mId > 1) {
                killDrift()
            } else {
                handleDrift()
            }
        }
    }

    /**
     * Setting body drift
     * @param drift drift value (0 - no drift, 1 - total drift)
     */
    fun setDrift(drift: Float) {
        mDrift = drift
    }

    /**
     * Returning body assigned to this body holder
     * @return body object
     */
    val body: Body
        get() = mBody

    /**
     * Handling drift
     */
    private fun handleDrift() {
        val forwardSpeed: Vector2 = forwardVelocity
        val lateralSpeed: Vector2 = lateralVelocity
        mBody.setLinearVelocity(forwardSpeed.x + lateralSpeed.x * mDrift, forwardSpeed.y + lateralSpeed.y * mDrift)
    }

    /**
     * Get extracted forward velocity vector
     * @return extracted forward vector
     */
    private val forwardVelocity: Vector2
        private get() {
            val currentNormal: Vector2 = mBody.getWorldVector(Vector2(0f, 1f))
            val dotProduct: Float = currentNormal.dot(mBody.getLinearVelocity())
            return multiply(dotProduct, currentNormal)
        }

    /**
     * Kill whole sideways velocity, and only apply forward velocity (no drift for the body)
     */
    fun killDrift() {
        mBody.setLinearVelocity(mForwardSpeed)
    }

    /**
     * Get extracted sideways velocity vector
     * @return extracted sideways velocity vector
     */
    private val lateralVelocity: Vector2
        private get() {
            val currentNormal: Vector2 = mBody.getWorldVector(Vector2(1f, 0f))
            val dotProduct: Float = currentNormal.dot(mBody.getLinearVelocity())
            return multiply(dotProduct, currentNormal)
        }

    /**
     * Determining if our body is moving forward or backward
     * @return
     */
    fun direction(): Int {
        val tolerance = 0.2f
        return if (localVelocity.y < -tolerance) {
            DIRECTION_BACKWARD
        } else if (localVelocity.y > tolerance) {
            DIRECTION_FORWARD
        } else {
            DIRECTION_NONE
        }
    }

    /**
     * Getting local velocity of a body
     * @return local velocity vector
     */
    private val localVelocity: Vector2
        private get() = mBody.getLocalVector(mBody.getLinearVelocityFromLocalPoint(Vector2(0f, 0f)))

    /**
     * Multiplying two vectors
     * @param a multiplier
     * @param v vector to multiply
     * @return multiplied vector
     */
    private fun multiply(a: Float, v: Vector2): Vector2 {
        return Vector2(a * v.x, a * v.y)
    }

    companion object {
        const val DIRECTION_NONE = 0
        const val DIRECTION_FORWARD = 1
        const val DIRECTION_BACKWARD = 2
        const val DRIFT_OFFSET = 1.0f
    }
}