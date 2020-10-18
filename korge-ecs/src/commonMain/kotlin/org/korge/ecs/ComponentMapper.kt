package org.korge.ecs

import com.soywiz.kds.IntArrayList
import kotlin.reflect.KClass

/**
 * ComponentMappers let you efficiently look up, add, and remove the corresponding [Component] instance for a given
 * entity.
 *
 * You acquire them using [World.componentMapperFor], and it's strongly recommended to cache the [ComponentMapper]
 * rather than retrieving it every tick.
 */
class ComponentMapper<T : Component> internal constructor(private val world: World, private val componentType: KClass<T>) {
    private val components = HashMap<Entity, T>()
    internal val addEntities = IntArrayList()
    internal val addComponents = ArrayList<T>()
    internal val removeEntities = IntArrayList()

    /**
     * Retrieve the component data of type [T] associated with [entity].
     *
     * @throws IllegalArgumentException if the entity doesn't have a component of this type attached.
     * @return a valid component
     */
    operator fun get(entity: Entity): T = components[entity] ?: throw IllegalArgumentException("Entity $entity does not have a $componentType")

    /**
     * Retrieves the component data of type [T] associated with [entity] if [entity] has a component of this type.
     *
     * @return a valid component, or null if the entity didn't have a component of this type.
     */
    fun tryGet(entity: Entity): T? = components[entity]

    /**
     * Adds the provided [component] data to the given [entity].
     *
     * If components are added mid-World [tick][World.tick], changes aren't applied until the end of the tick.
     * Otherwise the component is added immediately.
     *
     * If a component of type [T] already exists on [entity], the existing component will be overwritten.
     */
    fun addComponent(entity: Entity, component: T) {
        if (world.processing) {
            addEntities.add(entity)
            addComponents.add(component)
        } else {
            actuallyAddComponent(entity, component)
        }
    }

    internal fun actuallyAddComponent(entity: Entity, component: T) {
        if (components.put(entity, component) == null) {
            world.componentAdded(entity)
        } else {
            // Just overwrote a mapping?
        }
    }

    /**
     * Removes component data for the given [entity].
     *
     * If components are removed mid-World [tick][World.tick], changes aren't applied until the end of the tick.
     * Otherwise the component is removed immediately.
     *
     * If no component of type [T] exists on [entity], this method does nothing.
     */
    fun removeComponent(entity: Entity) {
        if (world.processing) {
            removeEntities.add(entity)
        } else {
            actuallyRemoveComponent(entity)
        }

    }
    internal fun actuallyRemoveComponent(entity: Entity) {
        if (components.remove(entity) != null) {
            world.componentRemoved(entity)
        } else {
            // Just removed a non-existing mapping? Could emit warning
        }
    }

    /**
     * Checks whether at this moment, [entity] has component data of type [T].
     *
     * Note that this does **not** check for whether a queued add/remove operation would affect the return value.
     *
     * @return true if this [ComponentMapper] currently has data that can be retrieved using [get].
     */
    fun hasEntity(entity: Entity): Boolean = entity in components

    internal fun destroy(entity: Entity, notifyWorld: Boolean) {
        if (components.remove(entity) != null) {
            if (notifyWorld) {
                world.componentRemoved(entity)
            }
        }
    }
}