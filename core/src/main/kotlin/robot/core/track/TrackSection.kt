package robot.core.track

import com.badlogic.gdx.math.Vector2
import ktx.math.minus
import ktx.math.plus
import ktx.math.vec2

class TrackSection(val points: Array<Vector2>, val widths: Array<Float>) {
    init {
        fixEdges()
    }

    lateinit var left: Array<Vector2>
    lateinit var right: Array<Vector2>

    private fun fixEdges() {
        left = Array(points.size) { vec2() }
        right = Array(points.size) { vec2() }
        for (i in 1..points.lastIndex) {
            val current = points[i - 1]
            val next = points[i]
            val direction = (next - current).nor()
            val dirLeft = direction.cpy().rotate90(1)
            dirLeft.scl(widths[i - 1] / 2f)
            val dirRight = direction.cpy().rotate90(-1)
            dirRight.scl(widths[i - 1] / 2f)
            left[i - 1].set(current + dirLeft)
            right[i - 1].set(current + dirRight)
        }
    }
}