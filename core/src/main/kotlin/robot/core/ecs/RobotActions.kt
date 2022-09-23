package robot.core.ecs

import eater.ai.GenericActionWithState
import eater.core.engine
import eater.ecs.components.Box2d
import eater.injection.InjectionContext
import eater.physics.forwardNormal
import ktx.ashley.allOf
import ktx.math.minus
import robot.core.ecs.components.Car
import robot.core.ecs.components.Robot
import robot.core.track.TrackMania
import robot.core.with
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
    val chaseMiddle = GenericActionWithState("Chase Player", {
        1f
    }, {

    }, { entity, robot, deltaTime ->

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
        if (angleDiff > 0f && angleDiff.absoluteValue > 10f)
            car.controlState = car.controlState.with(Car.right)
        else if (angleDiff < 0f && angleDiff.absoluteValue > 10f)
            car.controlState = car.controlState.with(Car.left)

        car.controlState = car.controlState.with(Car.forward)

    }, Robot::class)

}