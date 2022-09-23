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
import robot.core.GameConstants.GameHeight
import robot.core.GameConstants.GameWidth
import robot.core.ecs.UserData
import robot.core.ecs.systems.CarPhysicsSystem
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
                this.setContactListener(object: ContactListener {
                    override fun beginContact(contact: Contact) {
                        val bodyA = contact.fixtureA.body
                        val bodyB = contact.fixtureB.body

                    }

                    override fun endContact(contact: Contact) {
                        TODO("Not yet implemented")
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
            addSystem(UtilityAiSystem())
//            addSystem(UpdateActionsSystem())
        }
    }
}

fun Body.getUd() : UserData {
    return this.userData as UserData
}

sealed class ContactType {
    class PlayerAndRobot(val player: Entity, val robot: Entity): ContactType()
    class RobotAndRobot(val robotA: Entity, val robotB: Entity): ContactType()
    class PlayerAndWall(val player:Entity):ContactType()
    class RobotAndWall(val robot: Entity):ContactType()

    companion object {
        fun getContactType(contact: Contact):ContactType {
            val bodyAuserData = contact.fixtureA.body.getUd()
            val bodyBuserData = contact.fixtureB.body.getUd()
            when(bodyAuserData) {
                is UserData.Player -> {
                    when(bodyBuserData) {
                        is UserData.Robot -> return PlayerAndRobot(bodyAuserData.)
                    }
                }
                is UserData.Robot -> {}
                UserData.Wall -> {}
            }
            if(bodyAuserData is UserData.Wall || bodyBuserData is UserData.Wall) {

            }
        }
    }
}