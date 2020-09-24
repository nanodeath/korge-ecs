package org.korge.ecs

import com.soywiz.kds.IntArrayList

class ComponentMapper<T : Component>(private val world: World) {
    private val components = HashMap<Int, T>()
    internal val addEntities = IntArrayList()
    internal val addComponents = ArrayList<T>()
    internal val removeEntities = IntArrayList()

    operator fun get(idx: Int): T? = components[idx]

    fun addComponent(entity: Int, c: T) {
        if (world.processing) {
            addEntities.add(entity)
            addComponents.add(c)
        } else {
            actuallyAddComponent(entity, c)
        }
    }

    internal fun actuallyAddComponent(entity: Int, c: T) {
        if (components.put(entity, c) == null) {
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

    fun hasEntity(idx: Int): Boolean = components[idx] != null

    fun destroy(idx: Int) {
        destroy(idx, notifyWorld = true)
    }

    internal fun destroy(idx: Int, notifyWorld: Boolean) {
        if (components.remove(idx) != null) {
            if (notifyWorld) {
                world.componentRemoved(idx)
            }
        }
    }
}