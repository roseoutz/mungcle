rootProject.name = "mungcle"

pluginManagement {
    val kotlinVersion: String by settings
    val springBootVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        kotlin("plugin.jpa") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version "1.1.7"
        id("com.google.protobuf") version "0.9.4"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
    }
}

// Common modules
include("common:domain-common")
include("common:kafka-common")
include("common:grpc-client")

// Services
include("services:api-gateway")
include("services:pet-profile")
include("services:walks")
include("services:social")
