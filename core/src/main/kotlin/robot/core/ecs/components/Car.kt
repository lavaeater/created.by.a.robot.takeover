package robot.core.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

class Car : Component, Poolable {
    override fun reset() {
    }
}