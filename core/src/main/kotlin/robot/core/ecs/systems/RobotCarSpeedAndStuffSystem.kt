package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.Body
import eater.ecs.components.Box2d
import eater.injection.InjectionContext.Companion.inject
import eater.physics.addComponent
import ktx.ashley.allOf
import robot.core.GameConstants.RobotMaxDistance
import robot.core.ecs.components.Car
import robot.core.ecs.components.Player
import robot.core.ecs.components.Remove
import robot.core.ecs.components.Robot
import robot.core.track.TrackMania

class RobotAnnihilationSystem: IteratingSystem(allOf(Robot::class, Car::class, Box2d::class).get()) {
    val playerFamily = allOf(Player::class).get()
    val players get() = engine.getEntitiesFor(playerFamily)
    var playerBody: Body? = null
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (playerBody == null && players.any() && Box2d.has(players.first()))
            playerBody = Box2d.get(players.first()).body

        val robotBody = Box2d.get(entity).body

        val pPos = playerBody!!.worldCenter
        val rPos = robotBody.worldCenter

        if(pPos.dst(rPos) > RobotMaxDistance)
            entity.addComponent<Remove>()
    }
}

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
                MathUtils.clamp(rCar.maxForwardSpeed, pCar.maxForwardSpeed / 2f, pCar.maxForwardSpeed * 2f)
        }

    }

}