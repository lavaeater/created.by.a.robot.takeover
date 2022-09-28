package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalSystem
import eater.ecs.components.Box2d
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import ktx.log.debug
import ktx.log.info
import ktx.math.plus
import ktx.math.vec2
import robot.core.GameConstants.MinRobots
import robot.core.GameState
import robot.core.ecs.components.Player
import robot.core.ecs.components.Robot
import robot.core.ecs.createRobotCar
import robot.core.track.TrackMania

class EnemyNumbersControlSystem : IntervalSystem(0.25f) {
    val robots = allOf(Robot::class).get()
    val allRobots get() = engine.getEntitiesFor(robots)
    val trackMania by lazy { inject<TrackMania>() }

    override fun updateInterval() {
        if (GameState.raceStarted && Box2d.has(GameState.playerEntity)) {
            if (GameState.fillUpRobotsDelay > 0f)
                GameState.fillUpRobotsDelay -= interval

            if (GameState.fillUpRobotsDelay <= 0f) {
                if (allRobots.size() < GameState.minRobots) {
                    if((1..5).random() == 1) {
                        GameState.minRobots++
                        info { "newMin: ${GameState.minRobots}" }
                    }
                    val playerPointIndex =
                        trackMania.getIndexForPosition(Box2d.get(GameState.playerEntity).body.worldCenter.y - 150f)
                    createRobotCar(trackMania.track[playerPointIndex].center, 2f, 4f)
                }
            }
        }
    }
}