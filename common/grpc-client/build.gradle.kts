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
}
