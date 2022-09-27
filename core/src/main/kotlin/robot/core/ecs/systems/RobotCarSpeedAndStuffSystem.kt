package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import eater.ecs.components.Box2d
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import robot.core.GameConstants.RobotMaxOfPlayerSpeed
import robot.core.GameConstants.RobotMinOfPlayerSpeed
import robot.core.ecs.components.Car
import robot.core.ecs.components.Player
import robot.core.ecs.components.Robot
import robot.core.track.TrackMania

class RobotCarSpeedAndStuffSystem : IteratingSystem(allOf(Robot::class, Car::class, Box2d::class).get()) {

    val trackMania by lazy { inject<TrackMania>() }
    val playerFamily = allOf(Player::class).get()
    val players get() = engine.getEntitiesFor(playerFamily)
    var player: Entity? = null

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (player == null && players.any())
            player = players.first()

        /*

        What do we do?

        We check if the player is in front or behind of this particular robot car.

        If it is in front of the car, we speed up

        If it is behind the car, we lower top speed.
         */
        if(Box2d.has(player!!)) {
            val pPos = Box2d.get(player!!).body.worldCenter
            val pCar = Car.get(player!!)
            val pIndex = trackMania.getIndexForPosition(pPos.y)

            val rPos = Box2d.get(entity).body.worldCenter
            val rCar = Car.get(entity)
            val rIndex = trackMania.getIndexForPosition(rPos.y)
            if (rIndex < pIndex) {
                rCar.maxForwardSpeed += rCar.maxForwardSpeed * 0.1f
                rCar.maxDriveForce += rCar.maxDriveForce * 0.1f
            } else if (pIndex < rIndex) {
                rCar.maxForwardSpeed -= rCar.maxForwardSpeed * 0.1f
                rCar.maxDriveForce -= rCar.maxDriveForce * 0.1f
            }
            rCar.maxForwardSpeed =
                MathUtils.clamp(rCar.maxForwardSpeed, pCar.maxForwardSpeed * RobotMinOfPlayerSpeed, pCar.maxForwardSpeed * RobotMaxOfPlayerSpeed)
            rCar.maxDriveForce =
                MathUtils.clamp(rCar.maxDriveForce, pCar.maxDriveForce * RobotMinOfPlayerSpeed, pCar.maxDriveForce * RobotMaxOfPlayerSpeed)
        }

    }

}