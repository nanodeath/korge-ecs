package org.korge.ecs

/**
 * Utility class used by [World.createEntity] for convenient adding of components to new entities.
 */
class EntityBuilder internal constructor(val idx: Int, val world: World) {
    inline fun <reified T : Component> addComponent(c: T) {
        world.addComponent(idx, c)
    }
}