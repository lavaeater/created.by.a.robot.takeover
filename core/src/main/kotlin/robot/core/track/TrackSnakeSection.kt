package robot.core.track

import com.badlogic.gdx.math.Vector2
import ktx.math.minus
import ktx.math.plus
import ktx.math.vec2

data class TrackSnakeSection(val center: Vector2 = vec2(), val left: Vector2 = vec2(), val right: Vector2 = vec2(), var hasBody: Boolean = false) {
    fun fixSides(nextCenter: Vector2, width:Float ) {
        val direction = (nextCenter - center).nor()
        left.set(direction.cpy().rotate90(1).scl(width / 2f) + center)
        right.set(direction.cpy().rotate90(-1).scl(width / 2f) + center)
    }
}