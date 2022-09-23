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
     *
     *
     * We shall add and remove box2d bodies on the fly in this case...
     *
     * How, though?
     *
     * We shall add bodies for all sections within some typ of Y-region
     * Yes, if a left or right point is within say -100..500 of the cars y-coordinate, it should have a body added
     *
     * The track is right now 10000 meters long or something like that, in absolute y-units.
     *
     * So, if we create bodies for all of them, what happens then?
     *
     * And what happens if we simply create sections that are about 100 units in height?
     *
     * I would be good to have the track be in some kind of section form, instead of being in three different arrays.
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

    fun buildTrackSnake(
        startPoint: Vector2,
        sectionCount: Int,
        fidelity: Int,
        widthRange: ClosedFloatingPointRange<Float>,
        changeRange: IntRange): Array<TrackSnakeSection> {
        val points = buildTrack(startPoint, sectionCount, fidelity)
        var previousWidth = (widthRange.start + widthRange.endInclusive) / 2f
        val t = points.mapIndexed { i, p ->
            if (i < points.lastIndex) {
                MathUtils.clamp(previousWidth, widthRange.start, widthRange.endInclusive)
                previousWidth += changeRange.random() * 10f
                TrackSnakeSection(p).apply { fixSides(points[i + 1],) }
            } else {
                TrackSnakeSection()
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

    fun getTrack(
        sectionCount: Int,
        fidelity: Int,
        widthRange: ClosedFloatingPointRange<Float>,
        changeRange: IntRange
    ): TrackSection {
        val totalPoints = sectionCount * fidelity
        val points = buildTrack(vec2(), sectionCount, fidelity)
        val track = buildTrack(vec2(), sectionCount, fidelity)
        val widths = setWidths(totalPoints, widthRange, changeRange)
        return TrackSection(points, widths)
    }

}