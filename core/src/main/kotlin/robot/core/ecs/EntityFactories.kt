package robot.core.ecs

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import eater.ai.AiAction
import eater.ai.AiComponent
import eater.ai.GenericActionWithState
import eater.core.engine
import eater.core.world
import eater.ecs.components.Box2d
import eater.ecs.components.CameraFollow
import eater.ecs.components.ExplosionComponent
import eater.ecs.components.Remove
import eater.ecs.systems.RemoveAfter
import eater.physics.addComponent
import eater.physics.forwardVelocity
import ktx.ashley.EngineEntity
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.with
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.box2d.filter
import ktx.math.minus
import ktx.math.plus
import ktx.math.vec2
import robot.core.Assets
import robot.core.Box2dCategories
import robot.core.GameConstants
import robot.core.GameState
import robot.core.ecs.components.*
import kotlin.experimental.or

fun PickupType.getColor(): Color {
    return when (this) {
        PickupType.BarrelBomb -> Color.CYAN
        PickupType.GuidedMissile -> Color.RED
        PickupType.Health -> Color.GREEN
        PickupType.MachineGun -> Color.BLUE
        PickupType.Shotgun -> Color.PURPLE
        PickupType.SpeedBoost -> Color.YELLOW
        PickupType.Shield -> Color.ORANGE
    }
}

fun createPickup(position: Vector2, pickupType: PickupType) {
    engine().entity {
        withBox(pickupType.getColor())
        sensorBox(
            position,
            8f,
            8f,
            UserData.Pickup(this.entity, pickupType),
            Box2dCategories.pickups,
            Box2dCategories.pickupsCollideWith
        )
        with<Pickup>()
    }
}

fun explosionAt(position: Vector2, damage: Float, radius: Float) {
    /**
     * Simply add a sensor body at position
     *
     * And everything in that sensor body for the shooort timeperiod it exists
     * will be hit with damage and forces, I guess
     */
    Assets.explosion.play()
    engine().entity {
        with<Box2d> {
            body = sensorCirlce(
                position.cpy(),
                radius,
                UserData.Explosion(this@entity.entity, damage, radius),
                Box2dCategories.explosions,
                Box2dCategories.explosionsCollideWith
            )
        }
        with<RemoveAfter> {
            time = 0.2f
        }
        with<ExplosionComponent> {
            explosionTime = 0.2f
        }
//        with<Primitive> {
//            primitive = Circle(position, radius)
//        }
    }
}

data class ExplosionData(val position: Vector2, val damage: Float, val radius: Float)

fun explosionLater(position: Vector2, damage: Float, radius: Float) {
    /**
     * Simply add a sensor body at position
     *
     * And everything in that sensor body for the shooort timeperiod it exists
     * will be hit with damage and forces, I guess
     */
    GameState.explosionQueue.addLast(ExplosionData(position.cpy(), damage, radius))
}

fun PickupType.getBehavior(player: Boolean, direction: Vector2 = vec2()): AiAction {
    val robotCars = allOf(Box2d::class, Robot::class).get()
    val playerCars = allOf(Box2d::class, Player::class).get()
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
                    explosionAt(Box2d.get(entity).body.worldCenter, 50f, 20f)
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

        PickupType.GuidedMissile -> GenericActionWithState("Guided Missile", { 1f }, {}, { entity, state, delta ->
            state.flightTime -= delta
            if (state.flightTime > 0f) {
                val body = Box2d.get(entity).body
                if (state.armed) {
                    if (state.hasTarget) {
                        val directionToTarget = (state.target!!.worldCenter - body.worldCenter).nor()
                        val currentDirection = body.linearVelocity.nor()
                        currentDirection.lerp(directionToTarget, 0.25f)

//                        if ((body.angle * radiansToDegrees - currentDirection.angleDeg()) > 5f) {
//                            body.applyTorque(-150f, true)
//                        } else if ((body.angle * radiansToDegrees - currentDirection.angleDeg()) < -5f) {
//                            body.applyTorque(150f, true)
//                        }

                        body.setTransform(body.position, currentDirection.angleRad() - MathUtils.HALF_PI)
                        body.applyForce(currentDirection.scl(state.force), body.worldCenter, true)


                    } else {
                        val potentialTargets = engine().getEntitiesFor(if(player) robotCars else playerCars)
                        val target = potentialTargets.map { Box2d.get(it).body }
                            .sortedBy { it.worldCenter.dst(body.worldCenter) }.firstOrNull()
                        if (target != null) {
                            state.hasTarget = true
                            state.target = target
                        }
                        val forwardNormal = state.startDirection.cpy()
                        val currentSpeed = body.forwardVelocity().dot(forwardNormal)
                        if (currentSpeed < state.maxSpeed)
                            body.applyForce(forwardNormal.scl(state.force), body.worldCenter, true)
                    }
                } else {
                    val forwardNormal = state.startDirection.cpy()
                    val currentSpeed = body.forwardVelocity().dot(forwardNormal)
                    if (currentSpeed < state.maxSpeed)
                        body.applyForce(forwardNormal.scl(state.force), body.worldCenter, true)
                }
            } else {
                val body = Box2d.get(entity).body
                explosionAt(body.position, state.damage, state.radius)
                entity.addComponent<Remove>()
            }


        }, GuidedMissile::class)
        PickupType.MachineGun -> object: AiAction("Bullet") {
            override fun abort(entity: Entity) {
            }

            override fun act(entity: Entity, deltaTime: Float) {
                if(Box2d.has(entity)) {
                    val body = Box2d.get(entity).body
                    if(body.linearVelocity.len() < 50000f) {
                        val force = direction.cpy().scl(10000f)
                        body.applyForce(force, body.worldCenter, true)
                    }
                }
            }

        }
        PickupType.Shotgun -> object: AiAction("Bullet") {
            override fun abort(entity: Entity) {
            }

            override fun act(entity: Entity, deltaTime: Float) {
                if(Box2d.has(entity)) {
                    val body = Box2d.get(entity).body
                    if(body.linearVelocity.len() < 50000f) {
                        val force = direction.cpy().scl(10000f)
                        body.applyForce(force, body.worldCenter, true)
                    }
                }
            }

        }

        else -> object : AiAction("No Op") {
            override fun abort(entity: Entity) {

            }

            override fun act(entity: Entity, deltaTime: Float) {
            }
        }
    }
}

fun fireProjectile(position: Vector2, direction: Vector2, shooterSpeed: Float, weaponType: PickupType, player: Boolean) {
    when (weaponType) {
        PickupType.BarrelBomb -> {
            Assets.barrelBomb.play()
            engine().entity {
                withProjectile(position, .2f, UserData.Projectile(this.entity, weaponType))
                with<AiComponent> {
                    actions.add(weaponType.getBehavior(player))
                }
                with<HeightComponent>()
                with<SpriteComponent> {
                    texture = Assets.barrel
                    shadow = Assets.barrelShadow
                }
            }
        }

        PickupType.GuidedMissile -> {
            engine().entity {
                withProjectile(position, .2f, UserData.Projectile(this.entity, weaponType))
                with<AiComponent> {
                    actions.add(weaponType.getBehavior(player))
                }
                with<GuidedMissile> {
                    startDirection = direction
                    baseSpeed = shooterSpeed
                }
                with<SpriteComponent> {
                    texture = Assets.missile
                    shadow = Assets.missileShadow
                    shadowOffset = vec2(3f, -3f)
                }
            }
        }

        PickupType.MachineGun -> {
            engine().entity {
                withProjectile(position +  direction.cpy().scl(1.5f), .1f, UserData.Projectile(this.entity, weaponType), direction.cpy().scl(shooterSpeed + 500f))
                with<AiComponent> {
                    actions.add(weaponType.getBehavior(true, direction.cpy()))
                }
                with<SpriteComponent> {
                    texture = Assets.bullet
                    shadow = Assets.bullet
                    shadowOffset = vec2(0f, 0f)
                }
            }
        }
        PickupType.Shotgun -> {
            val left = direction.cpy().rotate90(1).scl(1.5f)
            val right = direction.cpy().rotate90(-1).scl(1.5f)
            engine().entity {
                withProjectile(position + left, .1f, UserData.Projectile(this.entity, weaponType), left.cpy().scl(1000f))
                with<AiComponent> {
                    actions.add(weaponType.getBehavior(true, left.cpy()))
                }
                with<SpriteComponent> {
                    texture = Assets.bullet
                    shadow = Assets.bullet
                    shadowOffset = vec2(0f, 0f)
                }
            }
            engine().entity {
                withProjectile(position + right, .1f, UserData.Projectile(this.entity, weaponType), right.cpy().scl(1000f))
                with<AiComponent> {
                    actions.add(weaponType.getBehavior(true, right.cpy()))
                }
                with<SpriteComponent> {
                    texture = Assets.bullet
                    shadow = Assets.bullet
                    shadowOffset = vec2(0f, 0f)
                }
            }
        }
        else -> {
            //No-OP
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

fun EngineEntity.withBox(tC: Color = Color.WHITE) {
    this.with<SpriteComponent> {
        tintColor = tC
        texture = Assets.box
        shadow = Assets.boxShadow
    }
}

fun createRobotCar(position: Vector2, width: Float, height: Float): Entity {
    return engine().entity {
        carBody(
            this, position, width, height, UserData.Robot(this.entity),
            Box2dCategories.cars,
            Box2dCategories.carsCollideWith
        )
        with<SpriteComponent> {
            texture = Assets.redCar
            shadow = Assets.redShadow
        }
        with<Robot>()
        with<AiComponent> {
            actions.add(RobotActions.chaseMiddle)
            actions.add(RobotActions.chasePlayer)
            actions.add(RobotActions.fireWeapon)
        }
        with<Car> {
            health = 100f
            maxTorque = CarBase.maxTorque
            maxDriveForce = CarBase.maxDriveForce * 0.1f
            acceleration = CarBase.acceleration * 0.1f
            maxForwardSpeed = CarBase.maxForwardSpeed * 0.1f
            immortalAdder = 0.1f
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
            Box2dCategories.carsCollideWith
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

fun EngineEntity.withProjectile(worldPos: Vector2, radius: Float, ud: UserData, startVelocity: Vector2 = vec2()) {
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
            linearVelocity.set(startVelocity)
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
    colliderMaskBits: Short
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
//                circle(10f, vec2(0f, 0f)) {
//                    isSensor = true
//                    filter {
//                        categoryBits = sensorBits
//                        maskBits = sensorMaskBits
//                    }
//                }
            }
        }
    }
}

