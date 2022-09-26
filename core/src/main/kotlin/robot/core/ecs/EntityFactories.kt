package robot.core.ecs

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import eater.ai.AiAction
import eater.ai.AiComponent
import eater.core.engine
import eater.core.world
import eater.ecs.components.Box2d
import eater.ecs.components.CameraFollow
import eater.physics.addComponent
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
import robot.core.ecs.components.*
import kotlin.experimental.or

fun createPickup(position: Vector2, pickupType: PickupType) {
    engine().entity {
        withBox()
        sensorBox(
            position,
            2f,
            2f,
            UserData.Pickup(this.entity, pickupType),
            Box2dCategories.pickups,
            Box2dCategories.pickupsCollideWith
        )
    }
}

fun explosionAt(position: Vector2) {
    /**
     * Simply add a sensor body at position
     *
     * And everything in that sensor body for the shooort timeperiod it exists
     * will be hit with damage and forces, I guess
     */
    sensorCirlce(position, 50f, UserData.Explosion(100f))
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
                    explosionAt(Box2d.get(entity).body.worldCenter)
                    entity.addComponent<Remove>()
                }
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

fun sensorCirlce(position: Vector2, radius: Float, ud: UserData) {
    world().body {
        userData = ud
        type = BodyDef.BodyType.DynamicBody
        position.set(position)
        circle(radius) {
            isSensor = true
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
        carBody(this, position, width, height, UserData.Robot(this.entity),
            Box2dCategories.cars,
            Box2dCategories.carsCollideWith,
            Box2dCategories.sensors,
            Box2dCategories.cars or Box2dCategories.projectiles or Box2dCategories.pickups)
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
            health = (10..20).random() * 10f
            maxTorque = (1..20).random() * 100f
            maxDriveForce = (8..200).random() * 1000f
            acceleration = (1..200).random() * 10f
            maxForwardSpeed = (5..20).random() * 1000f
        }
    }
}

fun createPlayerEntity(position: Vector2, width: Float, height: Float): Entity {
    /**
     * The player entity shall have
     * a sprite, a physical car body,
     * perhaps some wheels?
     */
    return engine().entity {
        carBody(
            this, position, width, height, UserData.Robot(this.entity),
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
        }
        with<CameraFollow>()
    }
}

fun EngineEntity.withProjectile(worldPos: Vector2, radius: Float, ud: UserData) {
    with<Box2d> {
        body = world().body {
            userData = ud
            type = BodyDef.BodyType.DynamicBody
            position.set(worldPos)
            circle(radius) {
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

