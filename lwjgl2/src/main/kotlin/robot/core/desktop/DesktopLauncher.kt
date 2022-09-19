@file:JvmName("DesktopLauncher")

package robot.core.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import robot.core.MainGame

/** Launches the desktop (LWJGL) application. */
fun main() {
    LwjglApplication(MainGame(), LwjglApplicationConfiguration().apply {
        title = "CreatedByARobotTakeover"
        width = 640
        height = 480
        intArrayOf(128, 64, 32, 16).forEach{
            addIcon("libgdx$it.png", Files.FileType.Internal)
        }
    })
}
