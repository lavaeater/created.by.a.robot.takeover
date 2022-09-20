package robot.core.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.math.vec2

class Car : Component, Poolable {
    val controlVector = vec2()
    override fun reset() {
        controlVector.setZero()
    }
}