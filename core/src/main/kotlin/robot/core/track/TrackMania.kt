package robot.core.track

import com.badlogic.gdx.math.CatmullRomSpline
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import ktx.math.random
import ktx.math.vec2

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

    fun getSection(startPoint: Vector2, noOfPoints: Int, yDistance: Float = 250f, xRange: ClosedFloatingPointRange<Float> = -250f..250f) : Array<Vector2> {
        val tmp = startPoint.cpy()
        return Array(noOfPoints) {
            /**
             * We always go up, so we add something to tmp
             */
            if(it == 0)
                startPoint.cpy()
            else
                tmp.set(tmp.x + xRange.random(), tmp.y + yDistance).cpy()
        }
    }

    fun setWidths(noOfPoints: Int): Array<Float> {
        val widthRange = 50f..500f
        val changeRange = -25f..25f
        var previousWidth = 250f
        return Array(noOfPoints) {
            if(it == 0)
                previousWidth
            else {
                previousWidth += changeRange.random()
                MathUtils.clamp(previousWidth, widthRange.start, widthRange.endInclusive)
            }
        }
    }

    fun buildTrack() : Array<Vector2> {
        val sectionCount = 10
        val fidelity = 10
        val totalPoints = sectionCount * fidelity
        val sampleSection = getSection(vec2(), sectionCount)
        val track = CatmullRomSpline(sampleSection, false)
        val points = Array(totalPoints){ vec2() }
        //Cache the points
        for(i in 1 until totalPoints) {
            track.valueAt(points[i], (i.toFloat() / (totalPoints.toFloat() - 1f)) )
        }
        /**
         * Do we calculate this on the fly?
         *
         * lets say we do a thousand points on the track... no, ten times the number of points
         */
        return points
    }

}