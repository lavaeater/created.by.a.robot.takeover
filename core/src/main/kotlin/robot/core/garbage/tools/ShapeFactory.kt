package robot.core.garbage.tools

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.topdowncar.game.Constants.PPM

object ShapeFactory {
    /**
     * Create basic physics rectangle
     * @param position recangle position
     * @param size rectangle size
     * @param type body type (static, dynamic or kinematic)
     * @param world [com.topdowncar.game.screens.PlayScreen.mWorld] used to control and add physics objects
     * @param density body density
     * @param sensor is body sensor or not
     * @return fully created body with parameters provided
     */
    fun createRectangle(
        position: Vector2,
        size: Vector2,
        type: BodyType,
        world: World,
        density: Float,
        sensor: Boolean
    ): Body {

        // define body
        val bdef = BodyDef()
        bdef.position.set(position.x / PPM, position.y / PPM)
        bdef.type = type
        val body: Body = world.createBody(bdef)

        // define fixture
        val shape = PolygonShape()
        shape.setAsBox(size.x / PPM, size.y / PPM)
        val fdef = FixtureDef()
        fdef.shape = shape
        fdef.density = density
        fdef.isSensor = sensor
        body.createFixture(fdef)
        shape.dispose()
        return body
    }
}