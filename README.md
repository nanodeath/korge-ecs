# KorGE-ECS

KorGE-ECS is an [ECS framework](https://en.wikipedia.org/wiki/Entity_component_system) for the Kotlin-based game engine
[KorGE](https://korge.org/).

ECS frameworks allow you to easily share data and logic across multiple entities.

There's a tiny "sample game" available at [korge-ecs-sample](https://github.com/nanodeath/korge-ecs-sample).

# Getting Started

## Dependencies

Head over to [bintray](https://bintray.com/nanodeath/korge/korge-ecs) and hit `SET ME UP!` for repo setup instructions.
Then scroll down to the `build settings` section for your toolchain, e.g. Gradle, which will include directions and a
version. You only need a single dependency on one package -- `korge-ecs`.

## World

First you need to define a `World`. The World contains all entities, components, and systems inside it; you'll likely
only need one.

```kotlin
val world = World()
```

You also need to invoke it every time you want the world to advance, probably something like this:

```kotlin
// In the scope of your KorGE Stage.
addUpdater { world.tick(it) }
```

This will cause your `world` to update every frame.

## Components

Components are your little bags of data that you can attach to your game entities. Stuff like position, graphics,
owner, velocity, etc. are all good candidates for a component.

Your components have only one hard requirement: they implement `Component`. A component that tracks position might look
like this:

```kotlin
class TransformComponent(var x: Double, var y: Double) : Component
```
We'll later be syncing the position from the `TransformComponent` to a KorGE `View` object. But how do we know which
`View` to sync it to? If only we could associate the view to the same entity...oh right.
```kotlin
class ViewComponent(val view: View) : Component
```

(We could have added a `View` field to the `TransformComponent`, but then `TransformComponent` would have data unrelated
to transforms! [Cohesion](https://en.wikipedia.org/wiki/Cohesion_(computer_science)) is always a good
practice, but it's even more important when designing components.)

One thing to keep in mind when designing your components is that they should contain data only -- you should have few
to no methods on your component classes. Instead, put that logic in your Systems. Also, you generally want to avoid
using inheritance in your components; you might subsequently expect to be able to filter by a superclass, but this
won't work.

### Registering Components

One annoying reality that's common for frameworks like this is for performance reasons, components must be 
**registered** with the World for them to function. This is simple enough, though:

```kotlin
world.registerComponentType<TransformComponent>()
world.registerComponentType<ViewComponent>()
```
(and no, the order in which you register components doesn't matter.)

## Entities

Entities are a vague name for the concept of a _thing_ in the game world that has a set of components attached to it.
Players, enemies, buildings, space stations, even bullets (though probably not the UI) are all examples of entities.

Let's create a moving logo, which could later be adapted into a player. The logo itself is an `Image` (subclass of 
`View`), so let's create that first.

```kotlin
// This assumes you have a korge.png image, like the KorGE "Hello World" template.
val image = image(resourcesVfs["korge.png"].readBitmap())
```
Next, let's create an entity that with this view attached, plus a position:

```kotlin
world.createEntity {
    addComponent(TransformComponent(0.0, 0.0))
    addComponent(ViewComponent(image))
}
```

`createEntity` is a helper method for make it easier to add components to new entities, but you can also
add/get/remove components using `ComponentMapper`, which will be covered later.

Let's sum up what we have so far. We have a world, which keeps track of all our entities and state. We've defined a
couple types of components. And we've constructed an entity, instantiated two components, and associated them to the
entity. Now all we need is something that uses the components. That something is the S of ECS: Systems.

## Systems

Systems encapsulate all of your game's actual logic; moving things, updating health, terminating enemies, synchronizing
state, etc. You might think that a systems will iterate over every entity every tick, but this isn't the case: systems
can "subscribe" to only relevant entities, entities that have a particular set of components. For example, if there's a
GravitySystem, it might only care about entities that have a `TransformComponent` or `RigidBodyComponent` component. A
`RenderingSystem` might only care if an entity has a `ViewComponent`. So it's important to think about the minimal set
of components that your system needs to operate.

Systems are automatically registered with the World provided in their constructor.

You have several choices for implementing your system as far as subclasses go:

<dl>
    <dt>System</dt>
    <dd>
        The most generic class, this is useful mainly for actions you want to occur every tick, but isn't related to
        entities. Drawing UI elements, perhaps.
    </dd>
    <dt>EntitySystem</dt>
    <dd>
        The base class for systems involving entities (that is, most systems).
    </dd>
    <dt>IteratingEntitySystem</dt>
    <dd>
        Similar to EntitySystems, but you can provide a filter for which entities you want to iterate over based on 
        which components they have. Your code is called once for each matching entity.
    </dd>
</dl>

`IteratingEntitySystem` will likely be your most common type of system, so let's build one now. The do-nothing example
 of this is as follows:

```kotlin
class NoOpSystem(world: World) : IteratingEntitySystem(world) {
    override fun processEntity(dt: TimeSpan, entity: Int) {
        // Do something?
    }
}
```

This system iterates over every entity (no filter provided in the superclass constructor) and does nothing in the 
`processEntity`. Of course, the main purpose of Systems is to iterate over entities and operate on them, right? For
that we need the component data associated with each entity. For that, we need component mappers.

ComponentMappers are retrieved from the World object for a given Component, like this:

```kotlin
val transformMapper = world.componentMapperFor<TransformComponent>()
```

ComponentMappers store all the component data for a given component type for **all** entities in the World. They're a 
little expensive to retrieve, so it's common to store them in a private property.

The main system we might want to build given `TransformComponent` and `ViewComponent` is a system that synchronizes
the transform position to the underlying view. That system might look like this:

```kotlin
class TransformSyncSystem(world: World) : IteratingEntitySystem(
        world,
        // Only iterate over entities that have these components
        TransformComponent::class, ViewComponent::class
) {
    // Mappers
    private val transformMapper = world.componentMapperFor<TransformComponent>()
    private val viewMapper = world.componentMapperFor<ViewComponent>()
    
    override fun processEntity(dt: TimeSpan, entity: Int) {
        // Retrieve transform and view data for our entity from the component mappers
        val transform = transformMapper[entity]
        val view = viewMapper[entity].view
        
        // Do what we want with them 
        view.xy(transform.x, transform.y)
    }
}
```

## Wrapping up

Your final "main" method would look like this:

```kotlin
val world = World()
world.registerComponentType<TransformComponent>()
world.registerComponentType<ViewComponent>()
TransformSyncSystem(world)
world.createEntity {
    addComponent(TransformComponent(0.0, 0.0))
    addComponent(ViewComponent(image))
}
```

or, using `apply`:
```kotlin
val world = World().apply {
    // Components
    registerComponentType<TransformComponent>()
    registerComponentType<ViewComponent>()
 
    // Systems
    TransformSyncSystem(this)
    
    // Entities
    createEntity {
        addComponent(TransformComponent(0.0, 0.0))
        addComponent(ViewComponent(image))
    }
}
addUpdater { world.tick(it) }
```

And that's it. Keep creating more components and systems and you'll have the makings of a game.

