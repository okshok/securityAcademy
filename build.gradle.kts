plugins {
    kotlin("jvm") version "2.0.0" apply false
    id("org.springframework.boot") version "3.3.2" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
    kotlin("plugin.spring") version "2.0.0" apply false
    kotlin("plugin.jpa") version "2.0.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
    
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "21"
        }
    }
    
    tasks.withType<JavaCompile> {
        targetCompatibility = "21"
    }
}