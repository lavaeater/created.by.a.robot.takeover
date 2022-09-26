package robot.core.ecs

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