package org.korge.ecs

/**
 * The component part of an ECS represents arbitrary data to be attached to entities.
 *
 * Implement this interface, then attach the resulting type to your [World] using [World.registerComponentType].
 */
interface Component