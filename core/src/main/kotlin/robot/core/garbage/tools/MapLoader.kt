package robot.core.garbage.tools

import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Disposable
import robot.core.garbage.Constants.MAP_NAME

class MapLoader(private val mWorld: World) : Disposable {
    private val mMap: TiledMap

    /**
     * Main MapLoader constructor
     * @param world [com.topdowncar.game.screens.PlayScreen.mWorld] used to control and add physics objects
     */
    init {
        mMap = TmxMapLoader().load(MAP_NAME)
        val walls: Array<RectangleMapObject> =
            mMap.getLayers().get(MAP_WALL).getObjects().getByType<RectangleMapObject>(
                RectangleMapObject::class.java
            )
        for (rObject in Array.ArrayIterator<RectangleMapObject>(walls)) {
            val rectangle: Rectangle = rObject.getRectangle()
            ShapeFactory.createRectangle(
                Vector2(
                    rectangle.getX() + rectangle.getWidth() / 2,
                    rectangle.getY() + rectangle.getHeight() / 2
                ),  // position
                Vector2(rectangle.getWidth() / 2, rectangle.getHeight() / 2),  // size
                BodyDef.BodyType.StaticBody, mWorld, OBJECT_DENSITY, false
            )
        }
    }// position
    // size
    /**
     * Return player main rectangle that is used in
     * [com.topdowncar.game.entities.Car.Car]
     * to position the player correctly
     * @return player rectangle received from map
     */
    val player: Body
        get() {
            val rectangle: Rectangle = mMap.getLayers().get(MAP_PLAYER).getObjects().getByType<RectangleMapObject>(
                RectangleMapObject::class.java
            ).get(0).getRectangle()
            return ShapeFactory.createRectangle(
                Vector2(
                    rectangle.getX() + rectangle.getWidth() / 2,
                    rectangle.getY() + rectangle.getHeight() / 2
                ),  // position
                Vector2(rectangle.getWidth() / 2, rectangle.getHeight() / 2),  // size
                BodyDef.BodyType.DynamicBody, mWorld, PLAYER_DENSITY, false
            )
        }

    override fun dispose() {
        mMap.dispose()
    }

    companion object {
        private const val MAP_WALL = "wall"
        private const val MAP_PLAYER = "player"
        private const val OBJECT_DENSITY = 1f
        private const val PLAYER_DENSITY = 0.4f
    }
}