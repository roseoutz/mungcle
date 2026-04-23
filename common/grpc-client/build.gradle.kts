import com.google.protobuf.gradle.*

plugins {
    kotlin("jvm")
    id("com.google.protobuf")
}

// proto 파일을 이 모듈에서 직접 컴파일. buf 대신 Gradle protobuf 플러그인 사용.

sourceSets {
    main {
        proto {
            srcDir("${rootDir}/proto")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${property("protobufVersion")}"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${property("grpcVersion")}"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${property("grpcKotlinVersion")}:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
                create("grpckt")
            }
            task.builtins {
                create("kotlin")
            }
        }
    }
}

dependencies {
    api("io.grpc:grpc-kotlin-stub:${property("grpcKotlinVersion")}")
    api("io.grpc:grpc-protobuf:${property("grpcVersion")}")
    api("io.grpc:grpc-stub:${property("grpcVersion")}")
    api("com.google.protobuf:protobuf-kotlin:${property("protobufVersion")}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    api("jakarta.annotation:jakarta.annotation-api:3.0.0")

    // Resilience4j Circuit Breaker — gRPC 클라이언트 공통 CB 래퍼
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
    implementation("io.github.resilience4j:resilience4j-kotlin:2.2.0")
    implementation("org.slf4j:slf4j-api:2.0.16")
    // @Component 사용을 위한 Spring Context (서비스가 컴포넌트 스캔으로 등록)
    implementation("org.springframework:spring-context:6.2.6")
    // @AutoConfiguration, @ConditionalOnClass 등 Spring Boot 자동 구성 애노테이션
    implementation("org.springframework.boot:spring-boot-autoconfigure:3.5.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
