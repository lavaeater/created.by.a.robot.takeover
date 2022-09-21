package robot.core.garbage.screens

import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.physics.box2d.World
import com.topdowncar.game.entities.Car

class PlayScreen : Screen {
    private val mBatch: SpriteBatch
    private val mWorld: World
    private val mB2dr: Box2DDebugRenderer
    private val mCamera: OrthographicCamera
    private val mViewport: Viewport
    private val mPlayer: Car
    private val mMapLoader: MapLoader

    /**
     * Base constructor for PlayScreen
     */
    init {
        mBatch = SpriteBatch()
        mWorld = World(GRAVITY, true)
        mB2dr = Box2DDebugRenderer()
        mCamera = OrthographicCamera()
        mCamera.zoom = DEFAULT_ZOOM
        mViewport = FitViewport(RESOLUTION.x / PPM, RESOLUTION.y / PPM, mCamera)
        mMapLoader = MapLoader(mWorld)
        mPlayer = Car(35.0f, 0.8f, 80, mMapLoader, Car.DRIVE_2WD, mWorld)
    }

    override fun show() {}
    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        handleInput()
        update(delta)
        draw()
    }

    /**
     * Handling user input and using [Car] class to assign direction values
     * Also handling other input, such as escape to quit the game and camera zoom
     */
    private fun handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            mPlayer.setDriveDirection(DRIVE_DIRECTION_FORWARD)
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            mPlayer.setDriveDirection(DRIVE_DIRECTION_BACKWARD)
        } else {
            mPlayer.setDriveDirection(DRIVE_DIRECTION_NONE)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            mPlayer.setTurnDirection(TURN_DIRECTION_LEFT)
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            mPlayer.setTurnDirection(TURN_DIRECTION_RIGHT)
        } else {
            mPlayer.setTurnDirection(TURN_DIRECTION_NONE)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit()
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            mCamera.zoom -= CAMERA_ZOOM
        } else if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            mCamera.zoom += CAMERA_ZOOM
        }
    }

    /**
     * Used only for graphic to draw stuff
     */
    private fun draw() {
        mBatch.setProjectionMatrix(mCamera.combined)
        mB2dr.render(mWorld, mCamera.combined)
    }

    /**
     * Main update method used for logic
     * @param delta delta time received from [PlayScreen.render] method
     */
    private fun update(delta: Float) {
        mPlayer.update(delta)
        mCamera.position.set(mPlayer.getBody().getPosition(), 0f)
        mCamera.update()
        mWorld.step(delta, VELOCITY_ITERATION, POSITION_ITERATION)
    }

    override fun resize(width: Int, height: Int) {
        mViewport.update(width, height)
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun dispose() {
        mBatch.dispose()
        mWorld.dispose()
        mB2dr.dispose()
        mMapLoader.dispose()
    }

    companion object {
        private const val CAMERA_ZOOM = 0.3f
    }
}