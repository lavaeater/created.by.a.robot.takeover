package robot.core.garbage.entities

import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import com.topdowncar.game.BodyHolder

class Car(
    private val mRegularMaxSpeed: Float,
    private val mDrift: Float,
    private val mAcceleration: Float,
    mapLoader: MapLoader,
    wheelDrive: Int,
    world: World
) : BodyHolder(mapLoader.getPlayer()) {
    private var mDriveDirection = DRIVE_DIRECTION_NONE
    private var mTurnDirection = TURN_DIRECTION_NONE
    private var mCurrentWheelAngle = 0f
    private val mAllWheels = Array<Wheel>()
    private val mRevolvingWheels = Array<Wheel>()
    private var mCurrentMaxSpeed = 0f

    /**
     * Base constructor for Car object
     * @param maxSpeed Maximum car speed
     * @param drift car drift value (0 - no drift, 1 absolute drift)
     * @param acceleration car acceleration amount
     * @param mapLoader [MapLoader] used to load get car position from the map
     * @param wheelDrive does this car have 4 wheel drive or 2 wheel drive
     * @param world [com.topdowncar.game.screens.PlayScreen.mWorld] used to control and add physics objects
     */
    init {
        getBody().setLinearDamping(LINEAR_DAMPING)
        getBody().getFixtureList().get(0).setRestitution(RESTITUTION)
        createWheels(world, wheelDrive)
    }

    /**
     * Method used to create wheel
     * @param world [com.topdowncar.game.screens.PlayScreen.mWorld] used to control and add physics objects
     * @param wheelDrive does this car have 4 wheel drive or 2 wheel drive
     */
    private fun createWheels(world: World, wheelDrive: Int) {
        for (i in 0 until WHEEL_NUMBER) {
            var xOffset = 0f
            var yOffset = 0f
            when (i) {
                Wheel.UPPER_LEFT -> {
                    xOffset = -WHEEL_OFFSET_X
                    yOffset = WHEEL_OFFSET_Y
                }

                Wheel.UPPER_RIGHT -> {
                    xOffset = WHEEL_OFFSET_X
                    yOffset = WHEEL_OFFSET_Y
                }

                Wheel.DOWN_LEFT -> {
                    xOffset = -WHEEL_OFFSET_X
                    yOffset = -WHEEL_OFFSET_Y
                }

                Wheel.DOWN_RIGHT -> {
                    xOffset = WHEEL_OFFSET_X
                    yOffset = -WHEEL_OFFSET_Y
                }

                else -> throw IllegalArgumentException("Wheel number not supported. Create logic for positioning wheel with number $i")
            }
            val powered = wheelDrive == DRIVE_4WD || wheelDrive == DRIVE_2WD && i < 2
            val wheel = Wheel(
                Vector2(getBody().getPosition().x * PPM + xOffset, getBody().getPosition().y * PPM + yOffset),
                WHEEL_SIZE,
                world,
                i,
                this,
                powered
            )
            if (i < 2) {
                val jointDef = RevoluteJointDef()
                jointDef.initialize(getBody(), wheel.getBody(), wheel.getBody().getWorldCenter())
                jointDef.enableMotor = false
                world.createJoint(jointDef)
            } else {
                val jointDef = PrismaticJointDef()
                jointDef.initialize(getBody(), wheel.getBody(), wheel.getBody().getWorldCenter(), Vector2(1f, 0f))
                jointDef.enableLimit = true
                jointDef.upperTranslation = 0f
                jointDef.lowerTranslation = jointDef.upperTranslation
                world.createJoint(jointDef)
            }
            mAllWheels.add(wheel)
            if (i < 2) {
                mRevolvingWheels.add(wheel)
            }
            wheel.setDrift(mDrift)
        }
    }

    /**
     * Used to process input received from GDX handled in [PlayScreen.handleInput]
     */
    private fun processInput() {
        val baseVector = Vector2(0f, 0f)
        if (mTurnDirection == TURN_DIRECTION_LEFT) {
            if (mCurrentWheelAngle < 0) {
                mCurrentWheelAngle = 0f
            }
            mCurrentWheelAngle =
                Math.min(WHEEL_TURN_INCREMENT.let { mCurrentWheelAngle += it; mCurrentWheelAngle }, MAX_WHEEL_ANGLE)
        } else if (mTurnDirection == TURN_DIRECTION_RIGHT) {
            if (mCurrentWheelAngle > 0) {
                mCurrentWheelAngle = 0f
            }
            mCurrentWheelAngle =
                Math.max(WHEEL_TURN_INCREMENT.let { mCurrentWheelAngle -= it; mCurrentWheelAngle }, -MAX_WHEEL_ANGLE)
        } else {
            mCurrentWheelAngle = 0f
        }
        for (wheel in Array.ArrayIterator(mRevolvingWheels)) {
            wheel.setAngle(mCurrentWheelAngle)
        }
        if (mDriveDirection == DRIVE_DIRECTION_FORWARD) {
            baseVector.set(0f, mAcceleration)
        } else if (mDriveDirection == DRIVE_DIRECTION_BACKWARD) {
            if (direction() === DIRECTION_BACKWARD) {
                baseVector.set(0f, -mAcceleration * REVERSE_POWER)
            } else if (direction() === DIRECTION_FORWARD) {
                baseVector.set(0f, -mAcceleration * BREAK_POWER)
            } else {
                baseVector.set(0f, -mAcceleration)
            }
        }
        // we currently set mCurrentMaxSpeed to regular speed, but we can use this to increase max
        // speed if user has turbo, or something like that. So we can apply this logic:
        // if (turboActive) {
        //    mCurrentMaxSpeed = mRegularMaxSpeed * 1.5f;
        //}
        mCurrentMaxSpeed = mRegularMaxSpeed
        if (getBody().getLinearVelocity().len() < mCurrentMaxSpeed) {
            for (wheel in Array.ArrayIterator(mAllWheels)) {
                if (wheel.isPowered) {
                    wheel.getBody().applyForceToCenter(wheel.getBody().getWorldVector(baseVector), true)
                }
            }
        }
    }

    /**
     * Setting drive direction either to forward or backward
     * @param driveDirection drive direction to set
     */
    fun setDriveDirection(driveDirection: Int) {
        mDriveDirection = driveDirection
    }

    /**
     * Setting turn direction either to left or right
     * @param turnDirection turn direction left or right
     */
    fun setTurnDirection(turnDirection: Int) {
        mTurnDirection = turnDirection
    }

    fun update(delta: Float) {
        super.update(delta)
        processInput()
        for (wheel in Array.ArrayIterator(mAllWheels)) {
            wheel.update(delta)
        }
    }

    companion object {
        const val DRIVE_2WD = 0
        const val DRIVE_4WD = 1
        const val DRIVE_DIRECTION_NONE = 0
        const val DRIVE_DIRECTION_FORWARD = 1
        const val DRIVE_DIRECTION_BACKWARD = 2
        const val TURN_DIRECTION_NONE = 0
        const val TURN_DIRECTION_LEFT = 1
        const val TURN_DIRECTION_RIGHT = 2
        private val WHEEL_SIZE: Vector2 = Vector2(16f, 32f)
        private const val LINEAR_DAMPING = 0.5f
        private const val RESTITUTION = 0.2f
        private const val MAX_WHEEL_ANGLE = 20.0f
        private const val WHEEL_TURN_INCREMENT = 1.0f
        private const val WHEEL_OFFSET_X = 64f
        private const val WHEEL_OFFSET_Y = 80f
        private const val WHEEL_NUMBER = 4
        private const val BREAK_POWER = 1.3f
        private const val REVERSE_POWER = 0.5f
    }
}