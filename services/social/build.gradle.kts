plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
}

dependencies {
    implementation(project(":common:domain-common"))
    implementation(project(":common:kafka-common"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // gRPC Server + Client (identity, pet-profile, walks 호출)
    implementation("net.devh:grpc-server-spring-boot-starter:3.1.0.RELEASE")
    implementation("net.devh:grpc-client-spring-boot-starter:3.1.0.RELEASE")
    implementation("io.grpc:grpc-kotlin-stub:${property("grpcKotlinVersion")}")
    implementation("io.grpc:grpc-protobuf:${property("grpcVersion")}")
    implementation("com.google.protobuf:protobuf-kotlin:${property("protobufVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    // Kafka Producer
    implementation("org.springframework.kafka:spring-kafka")

    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:${property("hypersistenceVersion")}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.3")

    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:${property("mockkVersion")}")
    testImplementation("org.testcontainers:postgresql:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:kafka:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:junit-jupiter:${property("testcontainersVersion")}")
}
