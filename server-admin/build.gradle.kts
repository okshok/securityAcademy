plugins {
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.2"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation(kotlin("reflect"))
    // macOS용 Netty DNS resolver 네이티브 라이브러리
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.100.Final:osx-aarch_64")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
java.sourceCompatibility = JavaVersion.VERSION_21