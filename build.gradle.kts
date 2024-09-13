plugins {
    kotlin("jvm") version "2.0.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0-rc-1")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.0-rc-1")
    implementation("io.ktor:ktor-client-cio:3.0.0-rc-1")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    implementation("ch.qos.logback:logback-classic:1.5.8")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("io.github.oshai:kotlin-logging:7.0.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}