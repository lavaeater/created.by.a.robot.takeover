@file:JvmName("Lwjgl3Launcher")

package robot.core.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import robot.core.RoboGame

/** Launches the desktop (LWJGL3) application. */
fun main() {
    Lwjgl3Application(RoboGame(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("CreatedByARobotTakeover")
        setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayModes().maxByOrNull { it.width }!!)
//        setWindowedMode(1920, 1080)
        setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
    })
}
