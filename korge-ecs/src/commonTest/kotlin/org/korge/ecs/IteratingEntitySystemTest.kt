package org.korge.ecs

import com.soywiz.klock.TimeSpan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IteratingEntitySystemTest {
    @Test
    fun iteratesThroughAllEntities() {
        val world = World()
        object : IteratingEntitySystem(world) {
            override fun processEntity(dt: TimeSpan, entity: Int) {
                assertTrue(entity in 1..3, "entity was $entity")
            }
        }
        world.createEntity()
        world.createEntity()
        world.createEntity()
        world.tick(TimeSpan(16.6))
    }

    @Test
    fun canRemoveWhileIterating() {
        val world = World()
        world.registerComponentType<ComponentA>()
        val componentAMapper = world.componentMapperFor<ComponentA>()
        val processed = mutableListOf<Int>()
        object : IteratingEntitySystem(world, ComponentA::class) {
            override fun processEntity(dt: TimeSpan, entity: Int) {
                if (entity % 2 == 0) {
                    componentAMapper.removeComponent(entity)
                }
                processed.add(entity)
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