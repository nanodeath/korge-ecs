package org.korge.ecs

class EntityBuilder(val idx: Int, val world: World) {
    inline fun <reified T : Component> addComponent(c: T) {
        world.addComponent(idx, c)
    }
}