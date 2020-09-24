package org.korge.ecs

import com.soywiz.klock.TimeSpan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IteratingEntitySystemTest {
    @Test
    fun `iterates through all entities`() {
        val world = World()
        object : IteratingEntitySystem(world) {
            override fun processEntity(dt: TimeSpan, idx: Int) {
                assertTrue(idx in 0..2)
            }
        }
        world.createEntity()
        world.createEntity()
        world.createEntity()
        world.tick(TimeSpan(16.6))
    }

    @Test
    fun `can remove while iterating`() {
        val world = World()
        world.registerComponentType<ComponentA>()
        val componentAMapper = world.componentMapperFor<ComponentA>()
        val processed = mutableListOf<Int>()
        object : IteratingEntitySystem(world, ComponentA::class) {
            override fun processEntity(dt: TimeSpan, idx: Int) {
                if (idx % 2 == 0) {
                    componentAMapper.removeComponent(idx)
                }
                processed.add(idx)
            }
        }
        repeat(5) {
            world.createEntity {
                addComponent(ComponentA())
            }
        }
        world.tick(TimeSpan(16.6))
        world.tick(TimeSpan(16.6))
        componentAMapper.addComponent(2, ComponentA(3))
        world.tick(TimeSpan(16.6))
        // This assertion is fragile because it assumes IntSet returns things in a particular order
        // but it seems to be working for now.
        assertEquals(listOf(
                1, 2, 3, 4, 5,
                1, 3, 5,
                1, 2, 3, 5), processed)
    }
}