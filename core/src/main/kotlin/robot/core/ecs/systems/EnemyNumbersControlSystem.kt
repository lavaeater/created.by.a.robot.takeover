package robot.core.ecs.systems

import com.badlogic.ashley.systems.IntervalSystem
import eater.ecs.components.Box2d
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import robot.core.GameConstants.MinRobots
import robot.core.GameState
import robot.core.ecs.components.Player
import robot.core.ecs.components.Robot
import robot.core.ecs.createRobotCar
import robot.core.track.TrackMania

class EnemyNumbersControlSystem : IntervalSystem(0.25f) {
    val robots = allOf(Robot::class).get()
    val players = allOf(Player::class).get()
    val allRobots get() = engine.getEntitiesFor(robots)
    val aPlayer by lazy { engine.getEntitiesFor(players).first() }
    val trackMania by lazy { inject<TrackMania>() }
    override fun updateInterval() {
        if (GameState.raceStarted && engine.getEntitiesFor(players).any()) {
            if (allRobots.size() < MinRobots) {
                val playerPointIndex = trackMania.getIndexForPosition(Box2d.get(aPlayer).body.worldCenter.y - 150f)
                createRobotCar(trackMania.track[playerPointIndex].center, 2f, 4f)
            }
        }
    }
}