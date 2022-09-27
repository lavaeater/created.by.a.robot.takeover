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
import ktx.math.minus
import ktx.math.random
import ktx.math.times
import robot.core.GameConstants.GameHeight
import robot.core.GameConstants.GameWidth
import robot.core.GameState
import robot.core.ecs.PickupType
import robot.core.ecs.components.Car
import robot.core.ecs.components.GuidedMissile
import robot.core.ecs.components.Remove
import robot.core.ecs.explosionAt
import robot.core.ecs.explosionLater
import robot.core.ecs.systems.*
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
                        if(GameState.raceStarted) {
                            when (val contactType = ContactType.getContactType(contact)) {
                                is ContactType.PlayerAndWall -> {
                                    //Take some damage
                                    val car = Car.get(contactType.player)
                                    car.health -= playerWallDamageRange.random()
                                }

                                is ContactType.RobotAndWall -> {
                                    if(Car.has(contactType.robot)) {
                                        val car = Car.get(contactType.robot)
                                        car.health -= robotAndWallDamageRange.random()
                                    }
                                }

                                is ContactType.RobotAndRobot -> {
                                    if(Car.has(contactType.robotA) && Car.has(contactType.robotB)) {
                                        Car.get(contactType.robotA).health -= robotAndRobotDamageRange.random()
                                        Car.get(contactType.robotB).health -= robotAndRobotDamageRange.random()
                                    }
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
                                is ContactType.CarAndExplosion -> {
                                    if(Car.has(contactType.car) && Box2d.has(contactType.car)) {
                                        val exp = contactType.explosion
                                        val explosionPosition = Box2d.get(exp).body.position
                                        val carBody = Box2d.get(contactType.car).body
                                        val car = Car.get(contactType.car)
                                        val radius = contactType.radius
                                        val maxDamage = contactType.damage

                                        val damageDist = 1f / (carBody.worldCenter.dst(explosionPosition) / radius)
                                        val actualDamage = damageDist * maxDamage
                                        car.health -= actualDamage

                                        val force =
                                            (carBody.worldCenter - explosionPosition).nor() * actualDamage * 100f
                                        carBody.applyLinearImpulse(force, carBody.worldCenter, true)
                                    }

                                }

                                is ContactType.CarAndProjectile -> {
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
            PickupType.SpeedBoost -> {
                Car.get(picker).maxForwardSpeed += Car.get(picker).maxForwardSpeed * 1.15f
            }

            else -> Car.get(picker).weapons.addLast(pickupType)
        }
    }

    private fun getEngine(): Engine {
        return PooledEngine().apply {
            addSystem(CameraUpdateSystem(inject(), inject()))
            addSystem(RenderSystem(inject()))
            addSystem(RenderPrimitives(inject()))
            addSystem(RenderExplosionSystem())
//            addSystem(PhysicsDebugRendererSystem(inject(), inject()))
            addSystem(CarPhysicsSystem())
            addSystem(RobotCarDeathSystem())
            addSystem(PlayerCarDeathSystem())
            addSystem(PlayerScoreSystem())
            addSystem(RemoveEntitySystem())
            addSystem(UtilityAiSystem())
            addSystem(EnemyNumbersControlSystem())
            addSystem(RemoveAfterSystem())
            addSystem(UpdateActionsSystem())
            addSystem(RobotCarSpeedAndStuffSystem())
            addSystem(RobotAnnihilationSystem())
        }
    }
}


