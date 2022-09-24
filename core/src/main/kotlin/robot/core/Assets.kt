package robot.core

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.toLocalFile

object Assets: DisposableRegistry by DisposableContainer() {
    private val blueCarTexture = Texture("cars/player-blue-single.png".toLocalFile(), true).alsoRegister()
    val blueCarRegion by lazy {
        TextureRegion(blueCarTexture, 0, 0, 16, 16)
    }
    private val carShadowTexture = Texture("cars/car-shadow.png".toLocalFile(), true).alsoRegister()
    val carShadowRegion by lazy {
        TextureRegion(carShadowTexture, 0, 0, 16, 16)
    }

    val splashBackground = Texture("splash/splash.png".toLocalFile(), true).alsoRegister()
}