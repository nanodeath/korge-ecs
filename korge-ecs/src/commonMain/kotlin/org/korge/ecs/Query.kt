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
class Query internal constructor(private val world: World, vararg components: KClass<out Component>) : Closeable {
    private val componentMappers: List<ComponentMapper<out Component>> = components.map { world.componentMapperFor(it) }
    private val internalEntities = IntSet()

    init {
        world.registerQuery(this)
    }

    val entities: Set<Int> get() = internalEntities

    override fun close() {
        world.unregisterQuery(this)
    }

    internal fun offer(entity: Int, removeIfApplicable: Boolean = false) {
        if (componentMappers.isEmpty() || componentMappers.all { it.hasEntity(entity) }) {
            internalEntities.add(entity)
        } else if (removeIfApplicable) {
            internalEntities.remove(entity)
        }
    }

    internal fun forget(entity: Int) {
        internalEntities.remove(entity)
    }
}