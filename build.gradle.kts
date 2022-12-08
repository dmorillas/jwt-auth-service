val ktor_version: String by project
val ktor_metrics_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val coroutines_version: String by project
val prometheus_version : String by project
val bcrypt_version : String by project
val jnanoid_version : String by project
val junit_version : String by project

plugins {
    application
    jacoco
    java
    kotlin("jvm") version "1.6.21"
                id("org.jetbrains.kotlin.plugin.serialization") version "1.6.21"
}

group = "co.morillas"
version = "0.0.1"
application {
    mainClass.set("co.morillas.auth.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    implementation("io.ktor:ktor-server:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")

    implementation("io.ktor:ktor-server-metrics:$ktor_metrics_version")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktor_metrics_version")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheus_version")

    implementation("io.ktor:ktor-network-tls-certificates:$ktor_version")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")

    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("io.ktor:ktor-client-json:$ktor_version")
    implementation("io.ktor:ktor-client-serialization-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$coroutines_version")

    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("at.favre.lib:bcrypt:$bcrypt_version")

    implementation("com.aventrix.jnanoid:jnanoid:$jnanoid_version")

    implementation("com.nimbusds:nimbus-jose-jwt:9.25.6")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junit_version")
    testImplementation("org.junit.vintage:junit-vintage-engine:$junit_version")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junit_version")

    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version")
    testImplementation("io.mockk:mockk:1.13.3")

    // Architecture checks
    testImplementation("com.tngtech.archunit:archunit:1.0.1")

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }
//    afterEvaluate {
//        classDirectories.setFrom(files(classDirectories.files.collect {
//            fileTree(dir: it, exclude: [
//                    "com/etermax/triviaskullz/sync/Main.class",
//                    "com/etermax/triviaskullz/sync/Player.class",
//                    "com/etermax/triviaskullz/sync/infrastructure/GameContext.class",
//                    "com/etermax/triviaskullz/sync/infrastructure/GameTimeProvider.class",
//                    "com/etermax/triviaskullz/sync/infrastructure/PlayerContext.class",
//                    "com/etermax/triviaskullz/sync/infrastructure/repositories/PlayerStateRepository.class",
//                    "com/etermax/triviaskullz/sync/infrastructure/senders/*Sender.class",
//                    "messages/**",
//            ])
//        }))
//    }

    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "co/morillas/auth/ApplicationKt.class",
                    "co/morillas/auth/GenerateJWKKt.class",
                    "co/morillas/auth/context/**",
                    "co/morillas/auth/plugins/**",
                    "co/morillas/auth/core/infrastructure/repository/user/**",
                )
            }
        })
    )
}