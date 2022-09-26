package robot.core.ecs

import com.badlogic.ashley.core.Entity

sealed class UserData {
    object Wall : UserData()
    class Explosion(val damage: Float): UserData()
    class Player(val player: Entity) : UserData()
    class Robot(val robot: Entity) : UserData()
    class Pickup(val pickup: Entity, val pickupType: PickupType) : UserData()
    class Projectile(val projectile: Entity, val weaponType: PickupType) : UserData()
}