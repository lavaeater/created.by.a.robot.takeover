package robot.core.ecs

sealed class PickupType(val name: String, val ammo: Int = 1, val rof: Float = 1f, val maxDamage: Int = 0) {
    object GuidedMissile : PickupType("Guided Missile")
    object MachineGun : PickupType("Machine Gun", 100, 15f, 5)
    object Shotgun : PickupType("Shotgun", 6, 2f, 10)
    object Health : PickupType("Health")
    object Shield : PickupType("Shield")
    object BarrelBomb : PickupType("Barrel Bomb", 1, 1f)
    object SpeedBoost : PickupType("Speed Boost")
    companion object {
        val allPickupTypes = listOf(Shotgun, MachineGun, GuidedMissile, BarrelBomb, Health, SpeedBoost, Shield)
    }
}