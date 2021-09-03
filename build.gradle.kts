import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#introduction
    // id("org.springframework.boot") version "2.3.5.RELEASE" apply false
    // https://docs.spring.io/dependency-management-plugin/docs/current/reference/html/
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.spring") version "1.4.30"
    `java-library`
}

ext["cloudVersion"] = "Hoxton.SR9"
ext["bootVersion"] = "2.3.5.RELEASE"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

allprojects {

    apply {
        plugin("io.spring.dependency-management")
    }

    group = "com.iminling"
    version = "2.2.0-SNAPSHOT"

    repositories {
        maven {
            setUrl("https://maven.aliyun.com/nexus/content/groups/public/")
        }
        mavenCentral()
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:2.3.5.RELEASE")
        }
        dependencies {
            dependency("org.projectlombok:lombok:1.16.20")
        }
    }

}

subprojects {
    apply{
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("java-library")
    }

    dependencies {
        //api(platform("org.springframework.boot:spring-boot-dependencies:2.3.5.RELEASE"))
        api(platform("org.jetbrains.kotlin:kotlin-bom"))
        compileOnly("org.projectlombok:lombok:1.16.20")
        annotationProcessor("org.projectlombok:lombok:1.16.20")
        testCompileOnly("org.projectlombok:lombok:1.16.20")
        testAnnotationProcessor("org.projectlombok:lombok:1.16.20")
    }

    tasks.withType<KotlinCompile> {
         kotlinOptions {
             freeCompilerArgs = listOf("-Xjsr305=strict")
             jvmTarget = "8"
         }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}