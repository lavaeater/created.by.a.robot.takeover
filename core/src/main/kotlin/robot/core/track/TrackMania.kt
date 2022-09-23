package robot.core.track

import com.badlogic.gdx.math.CatmullRomSpline
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import ktx.math.minus
import ktx.math.plus
import ktx.math.random
import ktx.math.vec2

class TrackSection(val points: Array<Vector2>, val widths: Array<Float>) {
    init {
        fixEdges()
    }

    lateinit var left: Array<Vector2>
    lateinit var right: Array<Vector2>

    private fun fixEdges() {
        left = Array(points.size) { vec2()}
        right = Array(points.size) { vec2()}
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

class TrackMania {
    /**
     * Will manage the race track. How?
     *
     * Let me tell you how...
     *
     * Do we need to periodically teleport everything to a zero-point in the game? Dunno.
     *
     *
     * The track will be a series of randomized points along a path
     *
     * These points will then be beziered into a smooth path, from which we shall build the actual track. Very cool indeed
     */

    fun getSection(
        startPoint: Vector2,
        noOfPoints: Int,
        yDistance: Float = 250f,
        xRange: ClosedFloatingPointRange<Float> = -250f..250f
    ): Array<Vector2> {
        val tmp = startPoint.cpy()
        return Array(noOfPoints) {
            /**
             * We always go up, so we add something to tmp
             */
            if (it == 0)
                startPoint.cpy()
            else
                tmp.set(tmp.x + xRange.random(), tmp.y + yDistance).cpy()
        }
    }

    fun setWidths(noOfPoints: Int, widthRange: ClosedFloatingPointRange<Float>, changeRange: IntRange): Array<Float> {
        var previousWidth = (widthRange.start + widthRange.endInclusive) / 2f
        return Array(noOfPoints) {
            if (it == 0)
                previousWidth
            else {
                previousWidth += changeRange.random() * 10f
                MathUtils.clamp(previousWidth, widthRange.start, widthRange.endInclusive)
            }
        }
    }

    fun buildTrack(startPoint: Vector2, sectionCount: Int, fidelity: Int): Array<Vector2> {
        val totalPoints = sectionCount * fidelity
        val sampleSection = getSection(vec2(), sectionCount)
        val track = CatmullRomSpline(sampleSection, false)
        val points = Array(totalPoints) { vec2() }
        //Cache the points
        points.first().set(startPoint)
        for (i in 1 until totalPoints) {
            track.valueAt(points[i], (i.toFloat() / (totalPoints.toFloat() - 1f)))
        }
        /**
         * Do we calculate this on the fly?
         *
         * lets say we do a thousand points on the track... no, ten times the number of points
         */
        return points
    }

    fun getTrack(sectionCount: Int, fidelity: Int, widthRange: ClosedFloatingPointRange<Float>, changeRange: IntRange): TrackSection {
        val totalPoints = sectionCount * fidelity
        val points = buildTrack(vec2(), sectionCount, fidelity)
        val widths = setWidths(totalPoints, widthRange, changeRange)
        return TrackSection(points, widths)
    }

}