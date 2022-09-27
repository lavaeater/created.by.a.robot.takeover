package robot.core.ecs

sealed class PickupType {
    object GuidedMissile : PickupType()
    object MachineGun : PickupType()
    object Shotgun : PickupType()
    object Health : PickupType()
    object BarrelBomb : PickupType()
    object SpeedBoost : PickupType()
    companion object {
        val allPickupTypes = listOf(GuidedMissile, BarrelBomb, Health, SpeedBoost)//, MachineGun, Shotgun,
    }
}