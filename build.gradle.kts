/*plugins {
    *//*id("org.springframework.boot") version "2.3.5.RELEASE"*//*
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("jvm") version "1.5.10"
    `java-library`
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:2.3.5.RELEASE"))
}

ext {
    set("spring.cloud-version", "Hoxton.SR9")
}

allprojects {
    group = "com.iminling"
    version = "2.2.0-SNAPSHOT"

    repositories {
        maven {
            setUrl("https://maven.aliyun.com/nexus/content/groups/public/")
        }
        mavenLocal()
    }

}*/
