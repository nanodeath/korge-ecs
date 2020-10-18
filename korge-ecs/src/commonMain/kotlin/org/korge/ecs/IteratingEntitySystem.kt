package org.korge.ecs

import com.soywiz.klock.TimeSpan
import kotlin.reflect.KClass

/**
 * Similar to [EntitySystem], but provides a means of processing entities one at a time.
 *
 * Note that if you're done with a [IteratingEntitySystem], you must [close] it to free up processing resources.
 */
abstract class IteratingEntitySystem(
    world: World,
    vararg requiredComponents: KClass<out Component>
) : EntitySystem(world, *requiredComponents) {
    /**
     * Process a single entity that has the required components.
     */
    abstract fun processEntity(dt: TimeSpan, entity: Entity)

    final override fun processEntities(dt: TimeSpan, entities: Set<Entity>) {
        entities.forEach { entityIdx -> processEntity(dt, entityIdx) }
    }
}