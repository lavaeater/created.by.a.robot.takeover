package robot.core.ecs

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import eater.ai.AiAction
import eater.ai.AiComponent
import eater.core.engine
import eater.core.world
import eater.ecs.components.Box2d
import eater.ecs.components.CameraFollow
import eater.physics.addComponent
import eater.physics.forwardVelocity
import ktx.ashley.EngineEntity
import ktx.ashley.entity
import ktx.ashley.with
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.box2d.filter
import ktx.math.vec2
import robot.core.Assets
import robot.core.Box2dCategories
import robot.core.GameConstants
import robot.core.ecs.components.*
import kotlin.experimental.or

fun createPickup(position: Vector2, pickupType: PickupType) {
    engine().entity {
        withBox()
        sensorBox(
            position,
            8f,
            8f,
            UserData.Pickup(this.entity, pickupType),
            Box2dCategories.pickups,
            Box2dCategories.pickupsCollideWith
        )
    }
}

fun explosionAt(position: Vector2, damage: Float, radius: Float) {
    /**
     * Simply add a sensor body at position
     *
     * And everything in that sensor body for the shooort timeperiod it exists
     * will be hit with damage and forces, I guess
     */
    engine().entity {
        with<Box2d> {
            body = sensorCirlce(
                position,
                radius,
                UserData.Explosion(this@entity.entity, damage, radius),
                Box2dCategories.explosions,
                Box2dCategories.explosionsCollideWith
            )
        }
        with<RemoveAfter> {
            time = 0.1f
        }
        with<Primitive> {
            primitive = Circle(position, radius)
        }
    }
}

fun PickupType.getBehavior(): AiAction {
    return when (this) {
        PickupType.BarrelBomb -> object : AiAction("Barrel Bomb") {
            override fun abort(entity: Entity) {
            }

            override fun act(entity: Entity, deltaTime: Float) {
                /**
                 * A barrel bomb must have a height component as well!
                 */
                val h = HeightComponent.get(entity)
                h.height += h.flightSpeed * deltaTime
                if (h.height > h.maxHeight)
                    h.flightSpeed = -h.flightSpeed
                else if (h.height < 0f) {
                    explosionAt(Box2d.get(entity).body.worldCenter, 100f, 25f)
                    entity.addComponent<Remove>()
                }

                //add some drag as well
                val body = Box2d.get(entity).body
                val forward = body.forwardVelocity()
                val speed = forward.len()
                val dragForceMagnitude = GameConstants.WindDragForceMagnitudeFactor * speed
                body.applyForce(forward.scl(dragForceMagnitude), body.worldCenter, true)
            }
        }

        PickupType.GuidedMissile -> object : AiAction("No Op") {
            override fun abort(entity: Entity) {

            }

            override fun act(entity: Entity, deltaTime: Float) {
            }
        }

        PickupType.Health -> object : AiAction("No Op") {
            override fun abort(entity: Entity) {

            }

            override fun act(entity: Entity, deltaTime: Float) {
            }
        }

        PickupType.MachineGun -> object : AiAction("No Op") {
            override fun abort(entity: Entity) {

            }

            override fun act(entity: Entity, deltaTime: Float) {
            }
        }

        PickupType.Shotgun -> object : AiAction("No Op") {
            override fun abort(entity: Entity) {

            }

            override fun act(entity: Entity, deltaTime: Float) {
            }
        }
    }
}

fun fireProjectile(position: Vector2, direction: Vector2, shooterSpeed: Float, weaponType: PickupType) {
    engine().entity {
        withProjectile(position, .2f, UserData.Projectile(this.entity, weaponType))
        with<AiComponent> {
            actions.add(weaponType.getBehavior())
        }
        when (weaponType) {
            PickupType.BarrelBomb -> {
                with<HeightComponent>()
                with<SpriteComponent> {
                    texture = Assets.barrel
                    shadow = Assets.barrelShadow
                }
            }

            PickupType.GuidedMissile -> {}
            PickupType.MachineGun -> {}
            PickupType.Shotgun -> {}
            else -> {}
        }
    }
}


fun EngineEntity.sensorBox(
    worldPosition: Vector2,
    width: Float,
    height: Float, ud: UserData,
    cb: Short,
    mb: Short
) {
    with<Box2d> {
        body = world().body {
            userData = ud
            type = BodyDef.BodyType.DynamicBody
            position.set(worldPosition)
            box(width, height) {
                filter {
                    categoryBits = cb
                    maskBits = mb
                }
                isSensor = true
                density = 0.5f
            }
        }
    }
}

fun sensorCirlce(worldPosition: Vector2, radius: Float, ud: UserData, cb: Short, mb: Short): Body {
    return world().body {
        userData = ud
        type = BodyDef.BodyType.DynamicBody
        position.set(worldPosition)
        circle(radius) {
            isSensor = true
            filter {
                categoryBits = cb
                maskBits = mb
            }
        }
    }
}

private fun EngineEntity.withBox2dBox(
    worldPos: Vector2,
    width: Float,
    height: Float,
    withSensor: Boolean,
    ud: UserData
) {
    this.with<Box2d> {
        body = world().body {
            userData = ud
            type = BodyDef.BodyType.DynamicBody
            position.set(worldPos)
            box(width, height) {
                density = 0.5f
            }
            if (withSensor)
                circle(width * 5f, vec2(0f, 0f)) {
                    isSensor = true
                }
        }
    }
}

fun EngineEntity.withBox() {
    this.with<SpriteComponent> {
        texture = Assets.box
        shadow = Assets.boxShadow
    }
}

fun createRobotCar(position: Vector2, width: Float, height: Float): Entity {
    return engine().entity {
        carBody(
            this, position, width, height, UserData.Robot(this.entity),
            Box2dCategories.cars,
            Box2dCategories.carsCollideWith,
            Box2dCategories.sensors,
            Box2dCategories.cars or Box2dCategories.projectiles or Box2dCategories.pickups
        )
        with<SpriteComponent> {
            texture = Assets.redCar
            shadow = Assets.redShadow
        }
        with<Robot>()
        with<AiComponent> {
            actions.add(RobotActions.chaseMiddle)
            actions.add(RobotActions.chasePlayer)
        }
        with<Car> {
            health = 100f
            maxTorque = CarBase.maxTorque
            maxDriveForce = CarBase.maxDriveForce
            acceleration = CarBase.acceleration
            maxForwardSpeed = CarBase.maxForwardSpeed
        }
//        with<Car> {
//            health = EnemyCarBase.health.random() * EnemyCarBase.healthFactor
//            maxTorque = EnemyCarBase.maxTorque.random() * EnemyCarBase.torqueFactor
//            maxDriveForce = EnemyCarBase.maxDriveForce.random() * EnemyCarBase.forceFactor
//            acceleration = EnemyCarBase.acceleration.random() * EnemyCarBase.accelerationFactor
//            maxForwardSpeed = EnemyCarBase.maxForwardSpeed.random() * EnemyCarBase.speedFactor
//        }
    }
}

object EnemyCarBase {
    val health = 10..20
    val maxTorque = 1..20
    val maxDriveForce = 8..200
    val acceleration = 1..200
    val maxForwardSpeed = 5..20

    val healthFactor = 10f
    val torqueFactor = 100f
    val forceFactor = 1000f
    val speedFactor = 1000f
    val accelerationFactor = 10f
}

object CarBase {
    val maxTorque = 300f
    val maxForwardSpeed = 5000f
    val maxBackwardSpeed = 200f
    val maxDriveForce = 1000f
    val currentDriveForce = 0f
    val acceleration = 200f
    val decceleration = 50f
}

fun createPlayerEntity(position: Vector2, width: Float, height: Float): Entity {
    /**
     * The player entity shall have
     * a sprite, a physical car body,
     * perhaps some wheels?
     */
    return engine().entity {
        carBody(
            this, position, width, height, UserData.Player(this.entity),
            Box2dCategories.cars,
            Box2dCategories.carsCollideWith,
            Box2dCategories.sensors,
            Box2dCategories.cars or Box2dCategories.projectiles or Box2dCategories.pickups
        )
        with<SpriteComponent> {
            texture = Assets.blueCar
            shadow = Assets.blueShadow
        }
        with<Player>()
        with<Car> {
            health = 100f
            maxTorque = CarBase.maxTorque
            maxDriveForce = CarBase.maxDriveForce
            acceleration = CarBase.acceleration
            maxForwardSpeed = CarBase.maxForwardSpeed
        }
        with<CameraFollow>()
    }
}

fun EngineEntity.withProjectile(worldPos: Vector2, radius: Float, ud: UserData) {
    val barrelBomb = (ud as UserData.Projectile).weaponType == PickupType.BarrelBomb
    with<Box2d> {
        body = world().body {
            userData = ud
            type = BodyDef.BodyType.DynamicBody
            position.set(worldPos)
            circle(radius) {
                filter {
                    categoryBits = if (barrelBomb) Box2dCategories.none else Box2dCategories.projectiles
                    maskBits = if (barrelBomb) Box2dCategories.none else Box2dCategories.projectilesCollideWith
                }
                density = 0.5f
            }
        }
    }
}


fun carBody(
    entity: EngineEntity,
    worldPos: Vector2,
    width: Float,
    height: Float,
    ud: UserData,
    colliderCategoryBits: Short,
    colliderMaskBits: Short,
    sensorBits: Short,
    sensorMaskBits: Short
) {

    entity.apply {
        with<Box2d> {
            body = world().body {
                userData = ud
                type = BodyDef.BodyType.DynamicBody
                position.set(worldPos)
                box(width, height) {
                    density = 0.5f
                    filter {
                        categoryBits = colliderCategoryBits
                        maskBits = colliderMaskBits
                    }
                }
                circle(10f, vec2(0f, 0f)) {
                    isSensor = true
                    filter {
                        categoryBits = sensorBits
                        maskBits = sensorMaskBits
                    }
                }
            }
        }
    }
}

