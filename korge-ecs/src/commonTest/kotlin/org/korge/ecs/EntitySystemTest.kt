package org.korge.ecs

import com.soywiz.klock.TimeSpan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EntitySystemTest {
    @Test
    fun tickProcessesEntities() {
        var executions = 0
        val world = World()
        object : EntitySystem(world) {
            override fun processEntities(dt: TimeSpan, entities: Set<Int>) {
                executions++
                assertEquals(TimeSpan(16.6), dt)
                assertTrue(entities.contains(1))
                assertTrue(entities.contains(2))
                assertTrue(entities.contains(3))
            }
        }
        world.createEntity()
        world.createEntity()
        world.createEntity()
        world.tick(TimeSpan(16.6))
        assertEquals(1, executions)
    }
}