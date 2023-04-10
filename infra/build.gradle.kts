plugins {
    kotlin("jvm") version "1.8.10"
    id("io.ktor.plugin") version "2.2.4"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
}

group = "whiteboard"
version = "0.0.1"

val exposed_version = "0.41.1"
val h2_version = "2.1.214"

application {
    //mainModule.set("infra")
    mainClass.set("whiteboard.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.2.4")
    implementation("io.ktor:ktor-server-auth-jvm:2.2.4")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.2.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.2.4")
    implementation("io.ktor:ktor-server-websockets-jvm:2.2.4")
    implementation("io.ktor:ktor-server-netty-jvm:2.2.4")
    implementation("ch.qos.logback:logback-classic:1.4.6")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("com.h2database:h2:$h2_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:2.2.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.8.10")
}
