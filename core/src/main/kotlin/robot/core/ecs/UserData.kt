package robot.core.ecs

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2

sealed class UserData {
    object Wall : UserData()
    class Explosion(val explosion: Entity, val damage: Float, val radius: Float): UserData()
    class Player(val player: Entity) : UserData()
    class Robot(val robot: Entity) : UserData()
    class Pickup(val pickup: Entity, val pickupType: PickupType) : UserData()
    class Projectile(val projectile: Entity, val weaponType: PickupType) : UserData()
}