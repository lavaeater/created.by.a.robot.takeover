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
import eater.ecs.components.Box2d
import eater.ecs.systems.CameraUpdateSystem
import eater.ecs.systems.PhysicsDebugRendererSystem
import eater.ecs.systems.UpdateActionsSystem
import eater.ecs.systems.UtilityAiSystem
import eater.injection.InjectionContext
import eater.physics.addComponent
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.log.debug
import ktx.log.info
import ktx.math.*
import robot.core.Assets
import robot.core.GameConstants.GameHeight
import robot.core.GameConstants.GameWidth
import robot.core.GameState
import robot.core.ecs.PickupType
import robot.core.ecs.components.Car
import robot.core.ecs.components.GuidedMissile
import robot.core.ecs.components.Player
import robot.core.ecs.components.Remove
import robot.core.ecs.explosionAt
import robot.core.ecs.explosionLater
import robot.core.ecs.systems.*
import robot.core.track.Pickup
import robot.core.track.TrackMania
import robot.core.ui.Hud
import space.earlygrey.shapedrawer.ShapeDrawer


object Context : InjectionContext() {
    val playerWallDamageRange = 1.5f..5f
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
                        if (GameState.raceStarted) {
                            when (val contactType = ContactType.getContactType(contact)) {
                                is ContactType.PlayerAndWall -> {
                                    //Take some damage
                                    val car = Car.get(contactType.player)
                                    car.takeDamage(playerWallDamageRange.random())
                                    Assets.bump.play()
                                }

                                is ContactType.RobotAndWall -> {
                                    if (Car.has(contactType.robot)) {
                                        val car = Car.get(contactType.robot)
                                        car.takeDamage(robotAndWallDamageRange.random())
                                    }
                                }

                                is ContactType.RobotAndRobot -> {
                                    if (Car.has(contactType.robotA) && Car.has(contactType.robotB)) {
                                        Car.get(contactType.robotA).takeDamage(robotAndRobotDamageRange.random())
//                                        Car.get(contactType.robotB).takeDamage(robotAndRobotDamageRange.random())
                                    }
                                }

                                is ContactType.PlayerAndRobot -> {
                                    if (Car.has(contactType.player) && Car.has(contactType.robot)) {
                                        Car.get(contactType.player).takeDamage(playerAndRobotDamageRange.random())
                                        Car.get(contactType.robot).takeDamage(robotAndPlayerDamageRange.random())
                                        Assets.bump.play()
                                    }
                                }

                                is ContactType.CarAndPickup -> handlePickup(
                                    contactType.car,
                                    contactType.pickup,
                                    contactType.pickupType
                                )

                                ContactType.NotRelevant -> {}
                                is ContactType.CarAndExplosion -> {
                                    if (Car.has(contactType.car) && Box2d.has(contactType.car)) {
                                        val exp = contactType.explosion
                                        val explosionPosition = Box2d.get(exp).body.position
                                        val carBody = Box2d.get(contactType.car).body
                                        val car = Car.get(contactType.car)
                                        val radius = contactType.radius
                                        val maxDamage = contactType.damage

                                        val damageDist =
                                            MathUtils.norm(radius, 0f, (carBody.worldCenter.dst(explosionPosition)))

                                        if (damageDist > 0f) {
                                            val actualDamage = damageDist * maxDamage
                                            if (Player.has(contactType.car))
                                                info { "$damageDist: $actualDamage" }
                                            car.takeDamage(actualDamage)

                                            val force =
                                                (carBody.worldCenter - explosionPosition).nor() * actualDamage * 50f
                                            carBody.applyLinearImpulse(force, carBody.worldCenter + vec2(0f, 1f), true)
                                        }
                                    }

                                }

                                is ContactType.CarAndProjectile -> {
                                    Assets.bump.play()
                                    if (GuidedMissile.has(contactType.projectile)) {
                                        val gm = GuidedMissile.get(contactType.projectile)
                                        if (gm.armed) {
                                            explosionLater(
                                                Box2d.get(contactType.projectile).body.worldCenter.cpy(),
                                                gm.damage,
                                                gm.radius
                                            )
                                            contactType.projectile.addComponent<Remove>()
                                        }
                                    } else {
                                        if(Car.has(contactType.car)) {
                                            val car = Car.get(contactType.car)
                                            car.takeDamage((1..contactType.weaponType.maxDamage).random().toFloat())
                                        }
                                        contactType.projectile.addComponent<Remove>()
                                    }
                                }

                                is ContactType.ProjectileAndAnything -> {
                                    if (GuidedMissile.has(contactType.projectile)) {
                                        val gm = GuidedMissile.get(contactType.projectile)
                                        explosionLater(
                                            Box2d.get(contactType.projectile).body.worldCenter.cpy(),
                                            gm.damage,
                                            gm.radius
                                        )
                                    }
                                    contactType.projectile.addComponent<Remove>()
                                }
                            }
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
            bindSingleton(TrackMania())
            bindSingleton(Hud(inject()))
        }
    }


    private fun handlePickup(picker: Entity, pickup: Entity, pickupType: PickupType) {
        Assets.powerUp.play()

        //1. Remove the pickup from the track.
        pickup.addComponent<Remove>()
        /**
         * Everything except for health just adds to the players collection of weapons.
         *
         * Player can have any number of weapons, in a list, which is a queue, and we pop that shit.
         */
        val car = Car.get(picker)
        car.lastPickup = pickupType
        when (pickupType) {
            PickupType.Health -> {
                car.health = MathUtils.clamp(car.health + 75f, 0f, 100f)
            }

            PickupType.Shield -> {
                car.addToImmortalTimer(10f)

            }

            PickupType.SpeedBoost -> {
                if (Car.has(picker)) {
                    car.maxForwardSpeed *= 1.25f
                    car.maxDriveForce *= 1.25f
                }
            }

            else -> car.weapons.addLast(pickupType)
        }
    }

    private fun getEngine(): Engine {
        return PooledEngine().apply {
            addSystem(CameraUpdateSystem(inject(), inject(), 0.1f))
            addSystem(CameraZoomSystem(inject()))
            addSystem(RenderSystem(inject()))
            addSystem(RenderPrimitives(inject()))
            addSystem(RenderExplosionSystem())
            addSystem(CarPhysicsSystem())
            addSystem(RobotCarDeathSystem())
            addSystem(PlayerCarDeathSystem())
            addSystem(PlayerWonSystem())
            addSystem(RemoveEntitySystem())
            addSystem(UtilityAiSystem())
            addSystem(EnemyNumbersControlSystem())
            addSystem(RemoveAfterSystem())
            addSystem(UpdateActionsSystem())
            addSystem(ImmortalitySystem())
            addSystem(RobotCarSpeedAndStuffSystem())
            addSystem(RobotAnnihilationSystem())
        }
    }
}


