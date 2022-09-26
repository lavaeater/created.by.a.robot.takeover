package robot.core.ecs

sealed class PickupType {
    object GuidedMissile : PickupType()
    object MachineGun : PickupType()
    object Shotgun : PickupType()
    object Health : PickupType()
    object BarrelBomb : PickupType()
    companion object {
        val allPickupTypes = listOf(Health, BarrelBomb) //GuidedMissile, MachineGun, Shotgun,
    }
}