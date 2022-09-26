package robot.core.injection

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Contact
import robot.core.ecs.PickupType

sealed class ContactType {
    class PlayerAndRobot(val player: Entity, val robot: Entity) : ContactType()
    class RobotAndRobot(val robotA: Entity, val robotB: Entity) : ContactType()
    class PlayerAndWall(val player: Entity) : ContactType()
    class RobotAndWall(val robot: Entity) : ContactType()
    object NotRelevant : ContactType()
    class CarAndPickup(val car: Entity, val pickup: Entity, val pickupType: PickupType) : ContactType()
    class CarAndExplosion(val car: Entity, val explosion: Entity, val damage: Float, val radius: Float) : ContactType()
    class CarAndProjectile(val car: Entity, val projectile: Entity, val weaponType: PickupType) : ContactType()
    class ProjectileAndAnything(val projectile: Entity) : ContactType()

    companion object {
        fun getContactType(contact: Contact): ContactType {
                return contact.getContactType()
        }
    }
}