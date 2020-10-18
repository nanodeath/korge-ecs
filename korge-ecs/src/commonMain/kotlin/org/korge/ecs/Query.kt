package org.korge.ecs

import com.soywiz.kds.IntSet
import com.soywiz.korio.lang.Closeable
import kotlin.reflect.KClass

/**
 * Queries are an internal structure that maintain a live list of all entities that have the desired components.
 * It's expensive because every time an entity is added, removed, or updated, every query must be re-evaluated.
 * For that reason, it's important to [close] queries when done with them, and [EntitySystems][EntitySystem] that use
 * them.
 */
class Query internal constructor(
    private val world: World,
    requiredComponents: Set<KClass<out Component>> = emptySet(),
    excludedComponents: Set<KClass<out Component>> = emptySet()
) : Closeable {
    private val required: List<ComponentMapper<out Component>> = requiredComponents.map { world.componentMapperFor(it) }
    // Excluded is not fully supported yet
    private val excluded: List<ComponentMapper<out Component>> = excludedComponents.map { world.componentMapperFor(it) }
    private val internalEntities = IntSet()

    val entities: Set<Entity> get() = internalEntities

    override fun close() {
        world.unregisterQuery(this)
    }

    internal fun offer(entity: Entity, removeIfApplicable: Boolean = false) {
        if (requiredMatch(entity)) {
            internalEntities.add(entity)
        } else if (removeIfApplicable) {
            internalEntities.remove(entity)
        }
    }

    private fun requiredMatch(entity: Entity): Boolean {
        // returns true if [required] is empty.
        return required.all { it.hasEntity(entity) }
    }

    internal fun forget(entity: Entity) {
        internalEntities.remove(entity)
    }
}
