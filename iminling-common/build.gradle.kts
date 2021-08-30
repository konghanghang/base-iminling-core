plugins {
    /*id("org.springframework.boot") version "2.3.5.RELEASE"*/
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("jvm") version "1.5.10"
    `java-library`
}

repositories {
    maven {
        setUrl("https://maven.aliyun.com/nexus/content/groups/public/")
    }
    mavenLocal()
}

group = "com.iminling"
version = "2.2.0-SNAPSHOT"

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:2.3.5.RELEASE"))
    api(platform("org.jetbrains.kotlin:kotlin-bom"))
    api(kotlin("stdlib"))
    compileOnly ("org.projectlombok:lombok:1.16.20")
    annotationProcessor ("org.projectlombok:lombok:1.16.20")
    testCompileOnly ("org.projectlombok:lombok:1.16.20")
    testAnnotationProcessor ("org.projectlombok:lombok:1.16.20")
    api ("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api ("org.apache.commons:commons-lang3:3.11")
    api ("commons-io:commons-io:2.6")
    api ("commons-codec:commons-codec:1.15")
    api ("org.apache.commons:commons-collections4:4.1")
    api ("com.google.guava:guava:30.0-jre")
    api ("org.bouncycastle:bcprov-jdk15on:1.64")
    api ("org.hashids:hashids:1.0.3")
    api ("org.apache.httpcomponents:httpmime:4.5.13")
    api ("org.slf4j:slf4j-api:1.7.30")
    api ("org.springframework:spring-web:5.2.10.RELEASE")
    api ("com.fasterxml.jackson.core:jackson-databind:2.11.3")
    api ("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.11.3")
    api ("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.3")
    api ("com.github.ben-manes.caffeine:caffeine:2.8.8")
    api ("org.junit.jupiter:junit-jupiter:5.6.3")
}
