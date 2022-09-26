package robot.core.injection

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.viewport.ExtendViewport
import eater.ecs.systems.CameraUpdateSystem
import eater.ecs.systems.PhysicsDebugRendererSystem
import eater.ecs.systems.UpdateActionsSystem
import eater.ecs.systems.UtilityAiSystem
import eater.injection.InjectionContext
import eater.physics.addComponent
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.math.random
import robot.core.GameConstants.GameHeight
import robot.core.GameConstants.GameWidth
import robot.core.ecs.PickupType
import robot.core.ecs.UserData
import robot.core.ecs.components.Car
import robot.core.ecs.components.Remove
import robot.core.ecs.systems.*
import robot.core.track.TrackMania
import robot.core.ui.Hud
import space.earlygrey.shapedrawer.ShapeDrawer


object Context : InjectionContext() {
    val playerWallDamageRange = 2.5f..7.5f
    val robotAndRobotDamageRange = 5f..10f
    val playerAndRobotDamageRange = 5f..10f
    val robotAndPlayerDamageRange = 10f..20f
    val robotAndWallDamageRange = 10f..20f

    private val shapeDrawerRegion: TextureRegion by lazy {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.drawPixel(0, 0)
        val texture = Texture(pixmap) //remember to dispose of later
        pixmap.disposeSafely()
        TextureRegion(texture, 0, 0, 1, 1)
    }

    fun initialize() {
        buildContext {
            bindSingleton(PolygonSpriteBatch())
            bindSingleton(OrthographicCamera())
            bindSingleton(
                ExtendViewport(
                    GameWidth,
                    GameHeight,
                    inject<OrthographicCamera>() as Camera
                )
            )
            bindSingleton(createWorld().apply {
                this.setContactListener(object : ContactListener {
                    override fun beginContact(contact: Contact) {
                        when (val contactType = ContactType.getContactType(contact)) {
                            is ContactType.PlayerAndWall -> {
                                //Take some damage
                                val car = Car.get(contactType.player)
                                car.health -= playerWallDamageRange.random()
                            }

                            is ContactType.RobotAndWall -> {
                                val car = Car.get(contactType.robot)
                                car.health -= robotAndWallDamageRange.random()
                            }

                            is ContactType.RobotAndRobot -> {
                                Car.get(contactType.robotA).health -= robotAndRobotDamageRange.random()
                                Car.get(contactType.robotB).health -= robotAndRobotDamageRange.random()
                            }

                            is ContactType.PlayerAndRobot -> {
                                Car.get(contactType.player).health -= playerAndRobotDamageRange.random()
                                Car.get(contactType.robot).health -= robotAndPlayerDamageRange.random()
                            }

                            is ContactType.CarAndPickup -> handlePickup(
                                contactType.car,
                                contactType.pickup,
                                contactType.pickupType
                            )

                            ContactType.NotRelevant -> {}
                        }
                    }

                    override fun endContact(contact: Contact) {
                    }

                    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
                    }

                    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
                    }
                })
            })
            bindSingleton(getEngine())
            bindSingleton(ShapeDrawer(inject<PolygonSpriteBatch>() as Batch, shapeDrawerRegion))
            bindSingleton(TrackMania().apply {
                this.track.addAll(this.getTrack(1000, 10, 150f..350f, -5..5))
                fixPickups(10)
            })
            bindSingleton(Hud(inject()))
        }
    }


    private fun handlePickup(picker: Entity, pickup: Entity, pickupType: PickupType) {
        //1. Remove the pickup from the track.
        pickup.addComponent<Remove>()
        /**
         * Everything except for health just adds to the players collection of weapons.
         *
         * Player can have any number of weapons, in a list, which is a queue, and we pop that shit.
         */
        when (pickupType) {
            PickupType.Health -> {
                Car.get(picker).health = MathUtils.clamp(Car.get(picker).health + 25f, 0f, 100f)
            }

            else -> Car.get(picker).weapons.addLast(pickupType)
        }
    }

    private fun getEngine(): Engine {
        return PooledEngine().apply {
            addSystem(CameraUpdateSystem(inject(), inject()))
            addSystem(RenderSystem(inject()))
            addSystem(PhysicsDebugRendererSystem(inject(), inject()))
            addSystem(CarPhysicsSystem())
            addSystem(CarDeathSystem())
            addSystem(RemoveEntitySystem())
            addSystem(UtilityAiSystem())
            addSystem(EnemyNumbersControlSystem())
//            addSystem(CarFollowSystem())
            addSystem(UpdateActionsSystem())
        }
    }
}

fun Body.getUd(): UserData {
    return this.userData as UserData
}

fun Contact.bothAreRobots(): Boolean {
    return this.fixtureA.body.userData is UserData.Robot && this.fixtureB.body.userData is UserData.Robot
}

fun Contact.getRobots(): Pair<UserData.Robot, UserData.Robot> {
    return Pair(this.fixtureA.body.userData as UserData.Robot, this.fixtureB.body.userData as UserData.Robot)
}

fun Contact.hasPlayer(): Boolean {
    return this.fixtureA.body.userData is UserData.Player || this.fixtureB.body.userData is UserData.Player
}

fun Contact.hasRobot(): Boolean {
    return this.fixtureA.body.userData is UserData.Robot || this.fixtureB.body.userData is UserData.Robot
}

fun Contact.hasWall(): Boolean {
    return this.fixtureA.body.userData is UserData.Wall || this.fixtureB.body.userData is UserData.Wall
}

fun Contact.hasPickup(): Boolean {
    return this.fixtureA.body.userData is UserData.Pickup || this.fixtureB.body.userData is UserData.Pickup
}

fun Contact.hasProjectile(): Boolean {
    return this.fixtureA.body.userData is UserData.Projectile || this.fixtureB.body.userData is UserData.Projectile
}

fun Contact.hasExplosion(): Boolean {
    return this.fixtureA.body.userData is UserData.Explosion || this.fixtureB.body.userData is UserData.Explosion
}

fun Contact.getPlayer(): UserData.Player {
    return if (this.fixtureA.body.userData is UserData.Player) this.fixtureA.body.userData as UserData.Player else this.fixtureB.body.userData as UserData.Player
}

fun Contact.getRobot(): UserData.Robot {
    return if (this.fixtureA.body.userData is UserData.Robot) this.fixtureA.body.userData as UserData.Robot else this.fixtureB.body.userData as UserData.Robot
}

fun Contact.getPickup(): UserData.Pickup {
    return if (this.fixtureA.body.userData is UserData.Pickup) this.fixtureA.body.userData as UserData.Pickup else this.fixtureB.body.userData as UserData.Pickup
}

fun Contact.getProjectile(): UserData.Projectile {
    return if (this.fixtureA.body.userData is UserData.Projectile) this.fixtureA.body.userData as UserData.Projectile else this.fixtureB.body.userData as UserData.Projectile
}

fun Contact.getExplosion(): UserData.Explosion {
    return if (this.fixtureA.body.userData is UserData.Explosion) this.fixtureA.body.userData as UserData.Explosion else this.fixtureB.body.userData as UserData.Explosion
}

fun Contact.getContactType(): ContactType {
    return if (this.bothAreRobots()) {
        val robots = this.getRobots()
        ContactType.RobotAndRobot(robots.first.robot, robots.second.robot)
    } else if (this.hasWall()) {
        if (this.hasPlayer())
            ContactType.PlayerAndWall(this.getPlayer().player)
        else if (this.hasRobot())
            ContactType.RobotAndWall(this.getRobot().robot)
        else
            ContactType.NotRelevant
    } else if (this.hasPickup()) {
        if (this.hasPlayer())
            ContactType.CarAndPickup(this.getPlayer().player, this.getPickup().pickup, this.getPickup().pickupType)
        else if (this.hasRobot())
            ContactType.CarAndPickup(this.getRobot().robot, this.getPickup().pickup, this.getPickup().pickupType)
        else
            ContactType.NotRelevant
    } else if (this.hasExplosion()) {
        if (this.hasPlayer())
            ContactType.CarAndExplosion(this.getPlayer().player, this.getExplosion().damage)
        else if (this.hasRobot())
            ContactType.CarAndExplosion(this.getRobot().robot, this.getExplosion().damage)
        else
            ContactType.NotRelevant
    } else if (this.hasProjectile()) {
        if (this.hasPlayer())
            ContactType.CarAndProjectile(
                this.getPlayer().player,
                this.getProjectile().projectile,
                this.getProjectile().weaponType
            )
        else if (this.hasRobot())
            ContactType.CarAndProjectile(
                this.getRobot().robot,
                this.getProjectile().projectile,
                this.getProjectile().weaponType
            )
        else
            ContactType.ProjectileAndAnything(this.getProjectile().projectile)
    } else if (this.hasPlayer() && this.hasRobot()) {
        ContactType.PlayerAndRobot(this.getPlayer().player, this.getRobot().robot)
    } else {
        ContactType.NotRelevant
    }
}

sealed class ContactType {
    class PlayerAndRobot(val player: Entity, val robot: Entity) : ContactType()
    class RobotAndRobot(val robotA: Entity, val robotB: Entity) : ContactType()
    class PlayerAndWall(val player: Entity) : ContactType()
    class RobotAndWall(val robot: Entity) : ContactType()
    object NotRelevant : ContactType()
    class CarAndPickup(val car: Entity, val pickup: Entity, val pickupType: PickupType) : ContactType()
    class CarAndExplosion(val car: Entity, val damage: Float) : ContactType()
    class CarAndProjectile(val car: Entity, val projectile: Entity, val weaponType: PickupType) : ContactType()
    class ProjectileAndAnything(val projectile: Entity) : ContactType()

    companion object {
        fun getContactType(contact: Contact): ContactType {
            return if (contact.fixtureA.isSensor || contact.fixtureB.isSensor) {
                NotRelevant
            } else {
                contact.getContactType()
            }
        }
    }
}