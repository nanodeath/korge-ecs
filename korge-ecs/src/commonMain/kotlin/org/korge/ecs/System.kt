package org.korge.ecs

import com.soywiz.klock.TimeSpan

interface System {
    fun process(dt: TimeSpan)
}