package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import eater.ecs.components.Box2d
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import robot.core.GameConstants.RobotMaxOfPlayerSpeed
import robot.core.GameConstants.RobotMinOfPlayerSpeed
import robot.core.GameState
import robot.core.ecs.components.Car
import robot.core.ecs.components.Player
import robot.core.ecs.components.Robot
import robot.core.track.TrackMania

class RobotCarSpeedAndStuffSystem : IntervalIteratingSystem(allOf(Robot::class, Car::class, Box2d::class).get(), 0.1f) {

    private val trackMania by lazy { inject<TrackMania>() }

    override fun processEntity(entity: Entity) {
        if(GameState.raceStarted && Box2d.has(GameState.playerEntity)) {
            /*

            What do we do?

            We check if the player is in front or behind of this particular robot car.

            If it is in front of the car, we speed up

            If it is behind the car, we lower top speed.
             */
            if (Box2d.has(GameState.playerEntity)) {
                val pPos = Box2d.get(GameState.playerEntity).body.worldCenter
                val pCar = Car.get(GameState.playerEntity)
                val pIndex = trackMania.getIndexForPosition(pPos.y)

                val rPos = Box2d.get(entity).body.worldCenter
                val rCar = Car.get(entity)
                val rIndex = trackMania.getIndexForPosition(rPos.y)
                if (rIndex < pIndex) {
                    rCar.maxForwardSpeed += rCar.maxForwardSpeed * 0.05f
                    rCar.maxDriveForce += rCar.maxDriveForce * 0.05f
                    rCar.acceleration += rCar.acceleration * 0.05f
                } else if (pIndex < rIndex) {
                    rCar.maxForwardSpeed -= rCar.maxForwardSpeed * 0.05f
                    rCar.maxDriveForce -= rCar.maxDriveForce * 0.05f
                    rCar.acceleration -= rCar.acceleration * 0.05f
                }
                rCar.maxForwardSpeed =
                    MathUtils.clamp(
                        rCar.maxForwardSpeed,
                        pCar.maxForwardSpeed * RobotMinOfPlayerSpeed,
                        pCar.maxForwardSpeed * RobotMaxOfPlayerSpeed
                    )
                rCar.maxDriveForce =
                    MathUtils.clamp(
                        rCar.maxDriveForce,
                        pCar.maxDriveForce * RobotMinOfPlayerSpeed,
                        pCar.maxDriveForce * RobotMaxOfPlayerSpeed
                    )
                rCar.acceleration =
                    MathUtils.clamp(
                        rCar.acceleration,
                        pCar.acceleration * RobotMinOfPlayerSpeed,
                        pCar.acceleration * RobotMaxOfPlayerSpeed
                    )
            }
        }
    }

}