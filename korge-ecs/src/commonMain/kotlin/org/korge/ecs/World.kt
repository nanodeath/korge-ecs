package org.korge.ecs

import com.soywiz.klock.TimeSpan
import kotlin.reflect.KClass

class World {
    private var entityCounter: Int = 0
    private val entityIndexes = LinkedHashSet<Int>()
    private val componentMappers = HashMap<KClass<out Component>, ComponentMapper<out Component>>()
    private val queries: MutableSet<Query> = LinkedHashSet()
    private val systems = mutableListOf<System>()
    var processing: Boolean = false
        private set

    fun createEntity(): Int {
        val idx = ++entityCounter
        entityIndexes.add(idx)
        queries.forEach { it.offer(idx) }
        return idx
    }

    fun createEntity(cb: EntityBuilder.() -> Unit): Int {
        val idx = ++entityCounter
        cb(EntityBuilder(idx, this))
        entityIndexes.add(idx)
        queries.forEach { it.offer(idx) }
        return idx
    }

    fun <T : Component> registerComponentType(componentClass: KClass<T>) {
        componentMappers[componentClass] = ComponentMapper(this, componentClass)
    }

    fun registerSystem(system: System) {
        systems.add(system)
    }

    fun tick(dt: TimeSpan) {
        processing = true
        systems.forEach { it.process(dt) }
        processing = false
        @Suppress("UNCHECKED_CAST")
        (componentMappers.values as Iterable<ComponentMapper<Component>>).forEach { mapper: ComponentMapper<Component> ->
            for (i in mapper.addEntities.indices) {
                // Conceptually we're zipping here, but this is more efficient than using Sequence#zip
                val entity = mapper.addEntities.getAt(i)
                val component = mapper.addComponents[i]
                mapper.actuallyAddComponent(entity, component)
            }
            for (i in mapper.removeEntities.indices) {
                val entity = mapper.removeEntities.getAt(i)
                mapper.actuallyRemoveComponent(entity)
            }
            mapper.addEntities.clear()
            mapper.addComponents.clear()
        }
    }

    inline fun <reified T : Component> registerComponentType() {
        registerComponentType(T::class)
    }

    fun <T : Component> componentMapperFor(c: KClass<T>): ComponentMapper<T> {
        @Suppress("UNCHECKED_CAST")
        return componentMappers[c] as ComponentMapper<T>?
                ?: throw IllegalArgumentException("Component not registered: $c")
    }

    inline fun <reified T : Component> componentMapperFor(): ComponentMapper<T> = componentMapperFor(T::class)

    fun destroyEntity(entity: Int) {
        componentMappers.values.forEach { it.destroy(entity, notifyWorld = false) }
        queries.forEach { it.forget(entity) }
        entityIndexes.remove(entity)
    }

    val entities get(): Set<Int> = entityIndexes

    internal fun registerQuery(query: Query): Boolean {
        if (queries.add(query)) {
            entities.forEach { query.offer(it) }
            return true
        }
        return false
    }

    internal fun unregisterQuery(query: Query): Boolean = queries.remove(query)

    internal fun componentAdded(entity: Int) {
        queries.forEach { it.offer(entity, removeIfApplicable = true) }
    }

    internal fun componentRemoved(entity: Int) {
        queries.forEach { it.offer(entity, removeIfApplicable = true) }
    }
}

inline fun <reified T : Component> World.addComponent(entity: Int, component: T) = componentMapperFor(T::class).addComponent(entity, component)
fun World.hasComponent(entity: Int, componentType: KClass<out Component>): Boolean = componentMapperFor(componentType).hasEntity(entity)
inline fun <reified T : Component> World.hasComponent(entity: Int): Boolean = hasComponent(entity, T::class)
fun <T : Component> World.getComponent(entity: Int, componentType: KClass<T>): T? = componentMapperFor(componentType)[entity]
inline fun <reified T : Component> World.getComponent(entity: Int): T? = getComponent(entity, T::class)