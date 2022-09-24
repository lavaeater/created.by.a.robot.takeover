package robot.core.injection

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.viewport.ExtendViewport
import eater.ecs.systems.CameraUpdateSystem
import eater.ecs.systems.PhysicsDebugRendererSystem
import eater.ecs.systems.UtilityAiSystem
import eater.injection.InjectionContext
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.math.random
import robot.core.GameConstants.GameHeight
import robot.core.GameConstants.GameWidth
import robot.core.ecs.UserData
import robot.core.ecs.components.Car
import robot.core.ecs.systems.CarDeathSystem
import robot.core.ecs.systems.CarPhysicsSystem
import robot.core.ecs.systems.RemoveEntitySystem
import robot.core.ecs.systems.RenderSystem
import robot.core.track.TrackMania
import space.earlygrey.shapedrawer.ShapeDrawer


object Context : InjectionContext() {
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
                                car.health -= (5f..15f).random()
                            }

                            is ContactType.RobotAndWall -> {
                                val car = Car.get(contactType.robot)
                                car.health -= (15f..35f).random()
                            }
                            is ContactType.RobotAndRobot -> {
                                Car.get(contactType.robotA).health -= (15f..50f).random()
                                Car.get(contactType.robotB).health -= (15f..50f).random()
                            }
                            is ContactType.PlayerAndRobot -> {
                                Car.get(contactType.player).health -= (15f..50f).random()
                                Car.get(contactType.robot).health -= (15f..50f).random()
                            }
                            else -> {}
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
            })
//            bindSingleton(Hud(inject()))
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
//            addSystem(UpdateActionsSystem())
        }
    }
}

fun Body.getUd(): UserData {
    return this.userData as UserData
}

sealed class ContactType {
    class PlayerAndRobot(val player: Entity, val robot: Entity) : ContactType()
    class RobotAndRobot(val robotA: Entity, val robotB: Entity) : ContactType()
    class PlayerAndWall(val player: Entity) : ContactType()
    class RobotAndWall(val robot: Entity) : ContactType()
    object NotRelevant : ContactType()

    companion object {
        fun getContactType(contact: Contact): ContactType {
            val bodyAuserData = contact.fixtureA.body.getUd()
            val bodyBuserData = contact.fixtureB.body.getUd()
            return when (bodyAuserData) {
                is UserData.Player -> {
                    when (bodyBuserData) {
                        is UserData.Robot -> PlayerAndRobot(bodyAuserData.player, bodyBuserData.robot)
                        is UserData.Wall -> PlayerAndWall(bodyAuserData.player)
                        else -> NotRelevant
                    }
                }

                is UserData.Robot -> {
                    when (bodyBuserData) {
                        is UserData.Player -> PlayerAndRobot(bodyBuserData.player, bodyAuserData.robot)
                        UserData.Wall -> RobotAndWall(bodyAuserData.robot)
                        is UserData.Robot -> RobotAndRobot(bodyAuserData.robot, bodyBuserData.robot)
                    }
                }

                UserData.Wall -> {
                    when (bodyBuserData) {
                        is UserData.Player -> PlayerAndWall(bodyBuserData.player)
                        is UserData.Robot -> RobotAndWall(bodyBuserData.robot)
                        else -> NotRelevant
                    }
                }
            }
        }
    }
}