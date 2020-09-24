package org.korge.ecs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorldTest {
    @Test
    fun `can create an entity`() {
        val world = World()
        val entity = world.createEntity {  }
        assertTrue(world.entities.contains(entity), "world contains $entity")
    }

    @Test
    fun `can get components`() {
        val world = World()
        world.registerComponentType<ComponentA>()
        val entity = world.createEntity {
            addComponent(ComponentA(3))
        }
        val componentA = world.componentMapperFor<ComponentA>()[entity]
        assertEquals(3, componentA?.counter)
    }
}