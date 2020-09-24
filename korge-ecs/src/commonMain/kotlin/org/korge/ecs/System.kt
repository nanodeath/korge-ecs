package org.korge.ecs

import com.soywiz.klock.TimeSpan

/**
 * Base interface that all ECS systems must implement.
 */
interface System {
    fun process(dt: TimeSpan)
}