package robot.core.ecs

sealed class PickupType(val name: String) {
    object GuidedMissile : PickupType("Guided Missile")
    object MachineGun : PickupType("Machine Gun")
    object Shotgun : PickupType("Shotgun")
    object Health : PickupType("Health")
    object BarrelBomb : PickupType("Barrel Bomb")
    object SpeedBoost : PickupType("Speed Boost")
    companion object {
        val allPickupTypes = listOf(SpeedBoost, GuidedMissile, BarrelBomb, Health)// , MachineGun, Shotgun,
    }
}