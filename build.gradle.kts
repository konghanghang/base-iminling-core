import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#introduction
    // id("org.springframework.boot") version "2.3.5.RELEASE" apply false
    // https://docs.spring.io/dependency-management-plugin/docs/current/reference/html/
    // id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.spring") version "1.4.30"
    `java-library`
    `maven-publish`
    signing
}

ext["cloudVersion"] = "Hoxton.SR9"
ext["bootVersion"] = "2.3.5.RELEASE"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}
//`java-platform`
/*publishing {
    publications {
        create<MavenPublication>("myPlatform") {
            artifactId = "base-iminling-core"
            from(components["javaPlatform"])
        }
    }
}*/

allprojects {

    apply {
        // plugin("io.spring.dependency-management")
        plugin("maven-publish")
        plugin("signing")
    }

    group = "com.iminling"
    version = "2.2.1-SNAPSHOT"

    repositories {
        maven {
            setUrl("https://maven.aliyun.com/nexus/content/groups/public/")
        }
        mavenCentral()
    }

    /*dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:2.3.5.RELEASE")
        }
        dependencies {
            dependency("org.projectlombok:lombok:1.16.20")
        }
    }*/

    publishing {
        repositories {
            maven {
                name = "mavenCentral"
                // change URLs to point to your repos, e.g. http://my.org/repo
                // val releasesRepoUrl = uri(layout.buildDirectory.dir("repos/releases"))
                val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                credentials {
                    // System.getenv放在.bash_profile中
                    // System.getProperty可以放在命令行也可以放在gradle.properties中
                    username = System.getProperty("SONATYPE_NEXUS_USERNAME")
                    password = System.getProperty("SONATYPE_NEXUS_PASSWORD")
                }
            }
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
        api(platform("org.springframework.boot:spring-boot-dependencies:2.3.5.RELEASE"))
        api(platform("org.jetbrains.kotlin:kotlin-bom"))
        compileOnly("org.projectlombok:lombok:1.16.20")
        annotationProcessor("org.projectlombok:lombok:1.16.20")
        testCompileOnly("org.projectlombok:lombok:1.16.20")
        testAnnotationProcessor("org.projectlombok:lombok:1.16.20")
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifactId = project.name
                from(components["java"])
                /*versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }*/
                pom {
                    name.set(project.name)
                    description.set("A concise description of my library")
                    url.set("https://www.example.com/library")
                    /*properties.set(mapOf(
                        "myProp" to "value",
                        "prop.with.dots" to "anotherValue"
                    ))*/
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("yslao")
                            name.set("yslao")
                            email.set("yslao@outlook.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/konghanghang/base-iminling-core.git")
                        developerConnection.set("scm:git:ssh://github.com/konghanghang/base-iminling-core.git")
                        url.set("https://github.com/konghanghang/base-iminling-core")
                    }
                }
            }
        }

        signing {
            sign(publishing.publications["mavenJava"])
        }

        tasks.javadoc {
            if (JavaVersion.current().isJava9Compatible) {
                (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
            }
        }
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