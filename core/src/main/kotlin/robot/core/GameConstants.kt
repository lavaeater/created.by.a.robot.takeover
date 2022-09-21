package robot.core

object GameConstants {
    const val GameHeight = 96f
    const val GameWidth = 128f
    const val PixelsPerMeter = 4f
    const val MetersPerPixel = 1f / PixelsPerMeter
    const val DragForceMagnitudeFactor = -1f
    const val MaxLateralImpulse = 5f
}

object Box2dCategories {
    private const val none: Short = 0
    const val cars: Short = 1
    const val terrain: Short = 2
    const val bullets: Short = 4


}