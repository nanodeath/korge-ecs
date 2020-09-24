package org.korge.ecs

import com.soywiz.klock.TimeSpan

/**
 * Base [System] implementation that operates over [World.entities].
 */
abstract class EntitySystem(protected val world: World) : System {
    init {
        world.registerSystem(this)
    }

    /**
     * Processes all entities in the attached [world].
     */
    protected abstract fun processEntities(dt: TimeSpan, entities: Set<Int>)

    final override fun process(dt: TimeSpan) {
        processEntities(dt, world.entities)
    }
}