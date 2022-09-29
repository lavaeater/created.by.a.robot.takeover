package robot.core.ecs

import eater.ai.GenericActionWithState
import eater.core.engine
import eater.ecs.components.Box2d
import eater.extensions.with
import eater.injection.InjectionContext
import eater.physics.forwardNormal
import eater.physics.forwardVelocity
import ktx.ashley.allOf
import ktx.math.minus
import ktx.math.plus
import robot.core.Assets
import robot.core.GameState
import robot.core.ecs.components.Car
import robot.core.ecs.components.Player
import robot.core.ecs.components.Robot
import robot.core.ecs.components.SpriteComponent
import robot.core.track.TrackMania
import kotlin.math.absoluteValue

object RobotActions {
    private val robotFamily = allOf(Robot::class).get()
    private val allTheRobots get() = engine().getEntitiesFor(robotFamily)

    /**
     * Aah, I just figured it out:
     *
     * The track of points that we have that are the "center" of the track
     * can be used to guid the enemy cars. They could try to drive as fast as they can towards
     * the next center point and everything is good.
     *
     * Or the player. We can simply check if the player is to the left or right of the forward
     * normal of the car and if it is, we turn left
     *
     *
     */
    val trackMania = InjectionContext.inject<TrackMania>()
    val playerFam = allOf(Player::class).get()

    val fireWeapon =
        GenericActionWithState("Fire Weapon", {
            val car = Car.get(it)
            if (car.weapons.any() || car.currentWeapon != null)
                0.7f
            else
                0f
        }, { _ -> },
            { entity, robot, delta ->
                val car = Car.get(entity)
                robot.shotTimer -= delta
                if(car.currentWeapon == null && car.weapons.any()) {
                    car.currentWeapon = car.weapons.removeFirst()!!
                    car.currentAmmo = car.currentWeapon!!.ammo
                }

                if(car.currentWeapon != null && robot.shotTimer < 0f) {
                    val weaponToFire = car.currentWeapon!!
                    car.currentAmmo--
                    if(car.currentAmmo <= 0)
                        car.currentWeapon = null

                    val robotBody = Box2d.get(entity).body
                    val forwardNormal = robotBody.forwardNormal()

                    val forwardSpeed = robotBody.forwardVelocity().dot(forwardNormal)
                    fireProjectile(
                        robotBody.worldCenter + forwardNormal.cpy().scl(2f),
                        forwardNormal,
                        forwardSpeed + 500f,
                        weaponToFire,
                        false
                    )
                    robot.shotTimer = 1f / weaponToFire.rof
                }
            }, Robot::class
        )

    val chasePlayer = GenericActionWithState("Chase The Player", {
        val robotPos = Box2d.get(it).body.worldCenter
        if (Box2d.has(GameState.playerEntity)) {
            val playerPos = Box2d.get(GameState.playerEntity).body.worldCenter
            if (robotPos.dst(playerPos) < 25f)
                0.6f
            else 0f
        } else
            0f
    }, {}, { entity, _, _ ->

        val sc = SpriteComponent.get(entity)
        sc.texture = Assets.greenCar

        val body = Box2d.get(entity).body
        val car = Car.get(entity)

        if (Box2d.has(GameState.playerEntity)) {
            val player = Box2d.get(GameState.playerEntity).body

            val targetDirection = (player.worldCenter - body.worldCenter)
            val bodyForward = body.forwardNormal()
            val angleDiff = bodyForward.angleDeg() - targetDirection.angleDeg()
            car.controlState = 0
            if (angleDiff > 0f && angleDiff.absoluteValue > 20f)
                car.controlState = car.controlState.with(Car.right)
            else if (angleDiff < 0f && angleDiff.absoluteValue > 20f)
                car.controlState = car.controlState.with(Car.left)

            car.controlState = car.controlState.with(Car.forward)
        }

    }, Robot::class)
    val chaseMiddle = GenericActionWithState("Chase The Track", {
        0.5f
    }, {

    }, { entity, robot, _ ->
        val sc = SpriteComponent.get(entity)
        sc.texture = Assets.redCar
        /**
         * get the next point to move towards. It should simply be some point with a higher y than the current
         * cars position
         *
         * No, we keep track of an index of course
         */
        /**
         * get the next point to move towards. It should simply be some point with a higher y than the current
         * cars position
         *
         * No, we keep track of an index of course
         */
        val body = Box2d.get(entity).body
        val car = Car.get(entity)
        if (robot.target.y < body.worldCenter.y) {
            robot.targetIndex = trackMania.getNextTarget(robot.targetIndex, body.worldCenter.y, robot.target)
        }
        val targetDirection = (robot.target - body.worldCenter)
        val bodyForward = body.forwardNormal()
        val angleDiff = bodyForward.angleDeg() - targetDirection.angleDeg()
        car.controlState = 0
        if (angleDiff > 0f && angleDiff.absoluteValue > 20f)
            car.controlState = car.controlState.with(Car.right)
        else if (angleDiff < 0f && angleDiff.absoluteValue > 20f)
            car.controlState = car.controlState.with(Car.left)

        car.controlState = car.controlState.with(Car.forward)

    }, Robot::class)

}