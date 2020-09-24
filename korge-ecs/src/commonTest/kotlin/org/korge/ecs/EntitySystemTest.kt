package org.korge.ecs

import com.soywiz.klock.TimeSpan
import kotlin.test.Test
import kotlin.test.assertEquals

class EntitySystemTest {
    @Test
    fun `tick processes entities`() {
        var executions = 0
        val world = World()
        object : EntitySystem(world) {
            override fun processEntities(dt: TimeSpan, entities: Set<Int>) {
                executions++
                assertEquals(TimeSpan(16.6), dt)
                assertEquals(setOf(1, 2, 3), entities)
            }
        }
        world.createEntity()
        world.createEntity()
        world.createEntity()
        world.tick(TimeSpan(16.6))
        assertEquals(1, executions)
    }
}