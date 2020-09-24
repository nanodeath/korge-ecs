package org.korge.ecs

import com.soywiz.klock.TimeSpan

abstract class EntitySystem(protected val world: World) : System {
    init {
        world.registerSystem(this)
    }

    protected abstract fun processEntities(dt: TimeSpan, entities: Set<Int>)

    final override fun process(dt: TimeSpan) {
        processEntities(dt, world.entities)
    }
}