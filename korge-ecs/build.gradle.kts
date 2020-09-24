apply<com.soywiz.korlibs.KorlibsPlugin>()

val korioVersion: String by project
val coroutinesVersion: String by project

dependencies {
    add("commonMainApi", "com.soywiz.korlibs.korio:korio:$korioVersion")
    add("commonMainApi", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
}