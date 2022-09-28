package robot.core.ecs.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import eater.ecs.components.Box2d
import robot.core.GameState

class CameraZoomSystem(private val camera: OrthographicCamera): EntitySystem() {

    val minZoom = 0.5f
    val maxZoom = 2.5f
    var minSpeed = 0f
    var maxSpeed = 0f

    override fun update(deltaTime: Float) {
        if(GameState.raceStarted && GameState.playerReady) {
            val playerBody = Box2d.get(GameState.playerEntity).body
            val linearVelocity = playerBody.linearVelocity.len()
            if(maxSpeed < linearVelocity) {
                maxSpeed = linearVelocity
            }

            val zoomFactor = MathUtils.norm(minSpeed, maxSpeed, linearVelocity)
            val targetZoom = MathUtils.lerp(minZoom, maxZoom, zoomFactor)
            camera.zoom = targetZoom
        }
    }
}