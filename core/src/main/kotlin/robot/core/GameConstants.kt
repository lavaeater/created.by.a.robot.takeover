package robot.core

object GameConstants {
    const val GameHeight = 64f
    const val GameWidth = 48f
    const val PixelsPerMeter = 4f
    const val MetersPerPixel = 1f / PixelsPerMeter
    const val DragForceMagnitudeFactor = -0.1f
    const val MaxLateralImpulse = 5f

    const val TimeStep = 1 / 60f
    const val VelIters = 16
    const val PosIters = 6

}

object Box2dCategories {
    private const val none: Short = 0
    const val cars: Short = 1
    const val terrain: Short = 2
    const val bullets: Short = 4


}