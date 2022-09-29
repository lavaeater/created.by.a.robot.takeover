package robot.core.track

sealed class Pickup {
    object GuidedMissile : Pickup()
    object BarrelBomb : Pickup()
    object Shotgun : Pickup()
    object MachingeGun : Pickup()
}