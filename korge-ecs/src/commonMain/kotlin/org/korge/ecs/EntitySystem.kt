package org.korge.ecs

import com.soywiz.klock.TimeSpan
import com.soywiz.korio.lang.Closeable
import kotlin.reflect.KClass

/**
 * Base [System] implementation that operates over a filtered set of [World.entities].
 */
abstract class EntitySystem(
    protected val world: World,
    vararg requiredComponents: KClass<out Component>
) : System, Closeable {
    private val query = Query(world, setOf(*requiredComponents)).also { world.registerQuery(it) }

    init {
        world.registerSystem(this)
    }

    /**
     * Processes all entities in the attached [world].
     */
    protected abstract fun processEntities(dt: TimeSpan, entities: Set<Entity>)

    final override fun process(dt: TimeSpan) {
        val query = requireNotNull(this.query)
        processEntities(dt, query.entities)
    }

    /**
     * Frees up processing resources when done when done with this system.
     */
    override fun close() {
        query.close()
    }
}