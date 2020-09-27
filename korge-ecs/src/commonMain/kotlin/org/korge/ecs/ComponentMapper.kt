package org.korge.ecs

import com.soywiz.kds.IntArrayList

/**
 * ComponentMappers let you efficiently look up, add, and remove the corresponding [Component] instance for a given
 * entity.
 *
 * You acquire them using [World.componentMapperFor], and it's strongly recommended to cache the [ComponentMapper]
 * rather than retrieving it every tick.
 */
class ComponentMapper<T : Component> internal constructor(private val world: World) {
    private val components = HashMap<Int, T>()
    internal val addEntities = IntArrayList()
    internal val addComponents = ArrayList<T>()
    internal val removeEntities = IntArrayList()

    operator fun get(entity: Int): T = components[entity]!!
    fun tryGet(entity: Int): T? = components[entity]

    fun addComponent(entity: Int, component: T) {
        if (world.processing) {
            addEntities.add(entity)
            addComponents.add(component)
        } else {
            actuallyAddComponent(entity, component)
        }
    }

    internal fun actuallyAddComponent(entity: Int, component: T) {
        if (components.put(entity, component) == null) {
            world.componentAdded(entity)
        } else {
            // Just overwrote a mapping?
        }
    }

    fun removeComponent(entity: Int) {
        if (world.processing) {
            removeEntities.add(entity)
        } else {
            actuallyRemoveComponent(entity)
        }

    }
    internal fun actuallyRemoveComponent(entity: Int) {
        if (components.remove(entity) != null) {
            world.componentRemoved(entity)
        } else {
            // Just removed a non-existing mapping? Could emit warning
        }
    }

    fun hasEntity(entity: Int): Boolean = entity in components

    fun destroy(entity: Int) {
        destroy(entity, notifyWorld = true)
    }

    internal fun destroy(entity: Int, notifyWorld: Boolean) {
        if (components.remove(entity) != null) {
            if (notifyWorld) {
                world.componentRemoved(entity)
            }
        }
    }
}