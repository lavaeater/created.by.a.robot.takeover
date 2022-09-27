package robot.core.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import eater.ecs.components.Box2d
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import robot.core.GameState
import robot.core.ecs.components.Player
import robot.core.track.TrackMania

class PlayerWonSystem:IteratingSystem(allOf(Player::class, Box2d::class).get()) {
    val trackMania by lazy { inject<TrackMania>() }
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if(GameState.raceStarted && Box2d.has(entity)) {
            val playerIndex = trackMania.getIndexForPosition(Box2d.get(entity).body.worldCenter.y)
            if(playerIndex == trackMania.track.lastIndex) {
                GameState.playerWon = true
            }
        }
    }
}