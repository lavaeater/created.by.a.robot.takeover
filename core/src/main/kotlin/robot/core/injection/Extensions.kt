package robot.core.injection

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Contact
import robot.core.ecs.UserData
import robot.core.ecs.explosionAt


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
        val explosion = this.getExplosion()
        if (this.hasPlayer())
            ContactType.CarAndExplosion(this.getPlayer().player, explosion.explosion, explosion.damage, explosion.radius)
        else if (this.hasRobot())
            ContactType.CarAndExplosion(this.getRobot().robot, explosion.explosion, explosion.damage, explosion.radius)
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