package robot.core.track

import com.badlogic.gdx.math.CatmullRomSpline
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import eater.core.world
import ktx.box2d.body
import ktx.box2d.chain
import ktx.math.plus
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

    val track = mutableListOf<SnakeTrackSection>()

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

    fun fixBodies(numberOfSections: Int, track: List<SnakeTrackSection>) {
        val startIndex = track.indexOfFirst { !it.hasBody }
        var endIndex = startIndex + numberOfSections - 1
        if (endIndex > track.lastIndex)
            endIndex = track.lastIndex

        val points = track.subList(startIndex, endIndex)
        world().body {
            chain(*points.map { it.left }.toTypedArray())
        }
        world().body {
            chain(*points.map { it.right }.toTypedArray())
        }
    }

    fun buildSnakeTrack(
        startPoint: Vector2,
        sectionCount: Int,
        fidelity: Int,
        widthRange: ClosedFloatingPointRange<Float>,
        changeRange: IntRange
    ): List<SnakeTrackSection> {
        val points = generateTrackPoints(startPoint, sectionCount, fidelity)
        var previousWidth = (widthRange.start + widthRange.endInclusive) / 2f
        return points.mapIndexed { i, p ->
            previousWidth =
                MathUtils.clamp(previousWidth + changeRange.random() * 10f, widthRange.start, widthRange.endInclusive)
            if (i < points.lastIndex) {
                SnakeTrackSection(p).apply { fixSides(points[i + 1], previousWidth) }
            } else {
                SnakeTrackSection(p).apply { fixSides(p + vec2(0f, 10f), previousWidth) }
            }
        }
    }

    fun generateTrackPoints(startPoint: Vector2, sectionCount: Int, fidelity: Int): Array<Vector2> {
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
    ): List<SnakeTrackSection> {
        val totalPoints = sectionCount * fidelity
        val snakeTrack = buildSnakeTrack(vec2(), sectionCount, fidelity, widthRange, changeRange)
        fixBodies(1000, snakeTrack)
        return snakeTrack
    }

    fun getNextTarget(targetIndex: Int, minY: Float, targetVector: Vector2): Int {
        var currentIndex = targetIndex
        var notFound = true
        while (currentIndex < track.lastIndex && notFound) {
            currentIndex++
            targetVector.set(track[currentIndex].center)
            if(targetVector.y > minY) {
                notFound = false
            }
        }
        return currentIndex
    }

}