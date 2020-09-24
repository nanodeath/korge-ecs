package org.korge.ecs

import com.soywiz.klock.TimeSpan
import com.soywiz.korio.lang.Closeable
import kotlin.reflect.KClass

abstract class IteratingEntitySystem(world: World, vararg requiredComponents: KClass<out Component>) : EntitySystem(world), Closeable {
    private val query = Query(world, *requiredComponents)

    abstract fun processEntity(dt: TimeSpan, idx: Int)

    override fun processEntities(dt: TimeSpan, entities: Set<Int>) {
        query.entities.forEach { entityIdx -> processEntity(dt, entityIdx) }
    }

    override fun close() {
        query.close()
    }
}