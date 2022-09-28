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
        val randRange = 0..49
        fun getPickup(): PickupType {
            val randVal = randRange.random()
            val key = odds.keys.filter { it.contains(randVal) }.first()
            return odds[key]!!
        }
        val odds = mapOf(0..4 to Shotgun, 5..9 to MachineGun, 10..14 to GuidedMissile, 15..19 to BarrelBomb, 20..29 to Health, 30..39 to SpeedBoost, 40..49 to Shield )

        val allPickupTypes = listOf(Shotgun, MachineGun, GuidedMissile, BarrelBomb, Health, SpeedBoost, Shield)
    }
}