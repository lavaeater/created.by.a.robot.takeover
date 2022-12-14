package robot.core.track

import com.badlogic.gdx.math.CatmullRomSpline
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import eater.core.world
import ktx.box2d.body
import ktx.box2d.chain
import ktx.box2d.filter
import ktx.math.plus
import ktx.math.random
import ktx.math.vec2
import robot.core.Box2dCategories
import robot.core.ecs.PickupType
import robot.core.ecs.UserData
import robot.core.ecs.createPickup

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

    fun fixPickups(odds: Int) {
        val range = 0..99
        val r = -5..5
        for (s in track) {
            if (range.random() < odds) {
                /**
                 * We need a pickup here
                 *
                 * ever pickup is an entity creation function that
                 * takes a position
                 */
                createPickup(s.center + vec2(r.random() * 2.5f, r.random() * 2.5f), PickupType.getPickup())
            }
        }

    }

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

    val trackBodies = mutableListOf<Body>()

    fun fixBodies(numberOfSections: Int, track: List<SnakeTrackSection>) {
        val startIndex = track.indexOfFirst { !it.hasBody }
        var endIndex = startIndex + numberOfSections - 1
        if (endIndex > track.lastIndex)
            endIndex = track.lastIndex

        val points = track.subList(startIndex, endIndex)
        trackBodies.add(world().body {
            userData = UserData.Wall
            chain(*points.map { it.left }.toTypedArray()) {
                filter {
                    categoryBits = Box2dCategories.terrain
                    maskBits = Box2dCategories.terrainCollidesWith
                }
            }
        })
        trackBodies.add(world().body {
            userData = UserData.Wall
            chain(*points.map { it.right }.toTypedArray()) {
                filter {
                    categoryBits = Box2dCategories.terrain
                    maskBits = Box2dCategories.terrainCollidesWith
                }

            }
        })
    }

    fun buildSnakeTrack(
        startPoint: Vector2,
        sectionCount: Int,
        fidelity: Int,
        widthRange: IntRange,
        changeRange: IntRange
    ): List<SnakeTrackSection> {
        val points = generateTrackPoints(startPoint, sectionCount, fidelity)
        var previousWidth = (widthRange.first * 10f + widthRange.last * 10f) / 2f
        val startWidth = previousWidth
        return points.mapIndexed { i, p ->
            previousWidth =
                MathUtils.clamp(previousWidth + changeRange.random() * 10f, widthRange.first * 10f, widthRange.last * 10f)
            if (i < points.lastIndex && i > 2) {
                SnakeTrackSection(p).apply { fixSides(points[i + 1], previousWidth) }
            } else {
                SnakeTrackSection(p).apply { fixSides(p + vec2(0f, 10f), startWidth) }
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
        points[1].set(startPoint + vec2(0f, 50f))
        points[2].set(startPoint + vec2(0f, 100f))
        for (i in 3 until totalPoints) {
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
        widthRange: IntRange,
        changeRange: IntRange
    ): List<SnakeTrackSection> {
        val snakeTrack = buildSnakeTrack(vec2(), sectionCount, fidelity, widthRange, changeRange)
        fixBodies(1000, snakeTrack)
        return snakeTrack
    }

    fun getIndexForPosition(maxY: Float): Int {
        val targetIndex = track.indexOfLast { it.center.y < maxY }
        return if (targetIndex < 0)
            0
        else
            targetIndex
    }

    fun getNextTarget(targetIndex: Int, minY: Float, targetVector: Vector2): Int {
        var currentIndex = targetIndex + 5
        var notFound = true
        while (currentIndex < track.lastIndex && notFound) {
            currentIndex++
            targetVector.set(track[currentIndex].center)
            if (targetVector.y > minY) {
                notFound = false
            }
        }
        return currentIndex
    }

    lateinit var polygons: Array<Polygon>
    fun clearTrack() {
        track.clear()
        for (b in trackBodies) {
            world().destroyBody(b)
        }
        trackBodies.clear()
    }

    fun createTrack(sectionCount: Int, fidelity: Int, widthRange: IntRange, changeRange: IntRange) {
        clearTrack()
        track.addAll(getTrack(sectionCount, fidelity, widthRange, changeRange))
        fixPickups(35)
        fixPolygons()
    }

    private fun fixPolygons() {
        polygons = (track.minus(track.last())).mapIndexed { i, t ->
            val o = track[i + 1]
            val points = FloatArray(8)
            points[0] = t.left.x
            points[1] = t.left.y
            points[2] = o.left.x
            points[3] = o.left.y
            points[4] = o.right.x
            points[5] = o.right.y
            points[6] = t.right.x
            points[7] = t.right.y
            Polygon(points)
        }.toTypedArray()
    }

}