package org.korge.ecs

import com.soywiz.klock.TimeSpan
import com.soywiz.korio.lang.Closeable
import kotlin.reflect.KClass

/**
 * Similar to [EntitySystem], but provides a means of processing entities one at a time and that have the necessary
 * components.
 *
 * Note that if you're discarding [IteratingEntitySystem], you must [close] it to free up processing resources.
 */
abstract class IteratingEntitySystem(
        world: World,
        vararg requiredComponents: KClass<out Component>
) : EntitySystem(world), Closeable {
    private val query = Query(world, *requiredComponents)

    /**
     * Process a single entity that has the required components.
     */
    abstract fun processEntity(dt: TimeSpan, idx: Int)

    override fun processEntities(dt: TimeSpan, entities: Set<Int>) {
        query.entities.forEach { entityIdx -> processEntity(dt, entityIdx) }
    }

    /**
     * Frees up processing resources when done when done with this system.
     */
    override fun close() {
        query.close()
    }
}