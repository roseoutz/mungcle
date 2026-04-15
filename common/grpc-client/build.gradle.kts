plugins {
    kotlin("jvm")
}

// buf generate로 생성된 코드를 담는 모듈.
// src/main/java/ + src/main/kotlin/ 은 buf가 자동 생성 (.gitignore에 포함).

dependencies {
    api("io.grpc:grpc-kotlin-stub:${property("grpcKotlinVersion")}")
    api("io.grpc:grpc-protobuf:${property("grpcVersion")}")
    api("com.google.protobuf:protobuf-kotlin:${property("protobufVersion")}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
}
