package robot.core.ecs.systems

import com.badlogic.ashley.systems.IntervalSystem
import robot.core.GameState

class PlayerScoreSystem: IntervalSystem(1f) {
    override fun updateInterval() {
        GameState.score++
    }

}