package robot.core

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.toLocalFile

object Assets: DisposableRegistry by DisposableContainer() {
    private val blueCarTexture = Texture("cars/player-blue-single.png".toLocalFile(), true).alsoRegister()
    val blueCar by lazy {
        TextureRegion(blueCarTexture, 0, 0, 16, 16)
    }
    private val carShadowTexture = Texture("cars/car-shadow.png".toLocalFile(), true).alsoRegister()
    val blueShadow by lazy {
        TextureRegion(carShadowTexture, 0, 0, 16, 16)
    }

    private val greenCarTexture = Texture("cars/player-green-single.png".toLocalFile(), true).alsoRegister()
    val greenCar by lazy {
        TextureRegion(greenCarTexture, 0, 0, 16, 16)
    }
    private val redCarTexture = Texture("cars/player-red-single.png".toLocalFile(), true).alsoRegister()
    val redCar by lazy {
        TextureRegion(redCarTexture, 0, 0, 16, 16)
    }
    private val redCarShadowTexture = Texture("cars/player-red-single-shadow.png".toLocalFile(), true).alsoRegister()
    val redShadow by lazy {
        TextureRegion(carShadowTexture, 0, 0, 16, 16)
    }

    private val boxTexture = Texture("props/box.png".toLocalFile(), true).alsoRegister()
    val box by lazy {
        TextureRegion(boxTexture, 0, 0, 16, 16)
    }
    private val boxShadowTexture = Texture("props/box-shadow.png".toLocalFile(), true).alsoRegister()
    val boxShadow by lazy {
        TextureRegion(boxShadowTexture, 0, 0, 16, 16)
    }

    private val barrelTexture = Texture("weapons/barrel.png".toLocalFile(), true).alsoRegister()
    val barrel by lazy {
        TextureRegion(barrelTexture, 0, 0, 16, 16)
    }
    private val barrelShadowTexture = Texture("weapons/barrel-shadow.png".toLocalFile(), true).alsoRegister()
    val barrelShadow by lazy {
        TextureRegion(barrelShadowTexture, 0, 0, 16, 16)
    }

    private val missileTexture = Texture("weapons/missile.png".toLocalFile(), true).alsoRegister()
    val missile by lazy {
        TextureRegion(missileTexture, 0, 0, 16, 16)
    }
    private val missileShadowTexture = Texture("weapons/missile-shadow.png".toLocalFile(), true).alsoRegister()
    val missileShadow by lazy {
        TextureRegion(missileShadowTexture, 0, 0, 16, 16)
    }

    val splashBackground = Texture("splash/splash.png".toLocalFile(), true).alsoRegister()
}