package org.korge.ecs

import com.soywiz.kds.IntSet
import com.soywiz.korio.lang.Closeable
import kotlin.reflect.KClass

class Query(private val world: World, vararg components: KClass<out Component>) : Closeable {
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
        if (componentMappers.all { it.hasEntity(entity) }) {
            internalEntities.add(entity)
        } else if (removeIfApplicable) {
            internalEntities.remove(entity)
        }
    }

    internal fun forget(entity: Int) {
        internalEntities.remove(entity)
    }
}