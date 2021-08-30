plugins {
    /*id("org.springframework.boot") version "2.3.5.RELEASE"*/
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("jvm") version "1.5.10"
    `java-library`
}

group = "com.iminling"
version = "2.2.0-SNAPSHOT"

repositories {
    maven {
        setUrl("https://maven.aliyun.com/nexus/content/groups/public/")
    }
    mavenLocal()
}

dependencies {
    api (project(":iminling-common"))
    compileOnly ("org.projectlombok:lombok:1.16.20")
    annotationProcessor ("org.projectlombok:lombok:1.16.20")
    testCompileOnly ("org.projectlombok:lombok:1.16.20")
    testAnnotationProcessor ("org.projectlombok:lombok:1.16.20")
    api ("org.springframework.boot:spring-boot-starter-web")
    api ("org.springframework.boot:spring-boot-starter-actuator")
    api ("com.baomidou:mybatis-plus-boot-starter:3.4.0")
    api ("org.hibernate.validator:hibernate-validator:6.1.6.Final")
    api ("org.aspectj:aspectjweaver:1.9.0")
    api ("cglib:cglib:3.1")
    api ("com.auth0:java-jwt:3.3.0")
    testImplementation ("org.springframework.boot:spring-boot-starter-test")
}