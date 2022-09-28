package robot.core

object GameConstants {
    const val GameWidth =42f
    const val GameHeight = (16f/9f) * GameWidth
    const val PixelsPerMeter = 4f
    const val MetersPerPixel = 1f / PixelsPerMeter
    const val DragForceMagnitudeFactor = -0.1f
    const val WindDragForceMagnitudeFactor = -0.05f
    const val MaxLateralImpulse = 5f

    const val TimeStep = 1 / 60f
    const val VelIters = 16
    const val PosIters = 6

    const val MinRobots = 15
    const val RobotMaxDistance = 500f
    const val RobotMaxOfPlayerSpeed = 1.15f
    const val RobotMinOfPlayerSpeed = 0.85f
}