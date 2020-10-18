package org.korge.ecs

import com.soywiz.klock.TimeSpan
import kotlin.reflect.KClass

class World {
    private var entityCounter: Int = 0
    private val entityIndexes = LinkedHashSet<Entity>()
    private val componentMappers = HashMap<KClass<out Component>, ComponentMapper<out Component>>()
    private val queries: MutableSet<Query> = LinkedHashSet()
    private val systems = mutableListOf<System>()
    var processing: Boolean = false
        private set

    fun createEntity(): Entity =
        (++entityCounter).also { entity: Entity ->
            entityIndexes.add(entity)
            queries.forEach { it.offer(entity) }
        }

    fun createEntity(cb: EntityBuilder.() -> Unit): Entity =
        (++entityCounter).also { entity: Entity ->
            cb(EntityBuilder(entity, this))
            entityIndexes.add(entity)
            queries.forEach { it.offer(entity) }
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
            mapper.removeEntities.clear()
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

    fun destroyEntity(entity: Entity) {
        componentMappers.values.forEach { it.destroy(entity, notifyWorld = false) }
        queries.forEach { it.forget(entity) }
        entityIndexes.remove(entity)
    }

    val entities get(): Set<Entity> = entityIndexes

    internal fun registerQuery(query: Query): Boolean {
        if (queries.add(query)) {
            entities.forEach { query.offer(it) }
            return true
        }
        return false
    }

    internal fun unregisterQuery(query: Query): Boolean = queries.remove(query)

    internal fun componentAdded(entity: Entity) {
        queries.forEach { it.offer(entity, removeIfApplicable = true) }
    }

    internal fun componentRemoved(entity: Entity) {
        queries.forEach { it.offer(entity, removeIfApplicable = true) }
    }

    internal companion object {
        internal val uninitialized = World()
    }
}

inline fun <reified T : Component> World.addComponent(entity: Entity, component: T) =
    componentMapperFor(T::class).addComponent(entity, component)

fun World.hasComponent(entity: Entity, componentType: KClass<out Component>): Boolean =
    componentMapperFor(componentType).hasEntity(entity)

inline fun <reified T : Component> World.hasComponent(entity: Entity): Boolean = hasComponent(entity, T::class)
fun <T : Component> World.getComponent(entity: Entity, componentType: KClass<T>): T =
    componentMapperFor(componentType)[entity]

inline fun <reified T : Component> World.getComponent(entity: Entity): T = getComponent(entity, T::class)