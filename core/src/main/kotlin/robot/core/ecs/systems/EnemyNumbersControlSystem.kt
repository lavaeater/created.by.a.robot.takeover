package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
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

class EnemyNumbersControlSystem : IntervalSystem(0.5f) {
    val robots = allOf(Robot::class).get()
    val players = allOf(Player::class).get()
    val allRobots get() = engine.getEntitiesFor(robots)
    lateinit var aPlayer: Entity
    val trackMania by lazy { inject<TrackMania>() }
    fun setPlayer() {
        if(!::aPlayer.isInitialized || !Box2d.has(aPlayer)) {
            aPlayer = engine.getEntitiesFor(players).first()
        }
    }
    override fun updateInterval() {
        if (GameState.raceStarted && engine.getEntitiesFor(players).any()) {
            if(GameState.fillUpRobotsDelay > 0f)
                GameState.fillUpRobotsDelay -= interval

            if(GameState.fillUpRobotsDelay <= 0f) {
                if (allRobots.size() < MinRobots) {
                    setPlayer()
                    val playerPointIndex = trackMania.getIndexForPosition(Box2d.get(aPlayer).body.worldCenter.y - 150f)
                    createRobotCar(trackMania.track[playerPointIndex].center, 2f, 4f)
                }
            }
        }
    }
}