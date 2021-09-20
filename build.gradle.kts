import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#introduction
    // id("org.springframework.boot") version "2.3.5.RELEASE" apply false
    // https://docs.spring.io/dependency-management-plugin/docs/current/reference/html/
    // id("io.spring.dependency-management") version "1.0.11.RELEASE"
    // https://docs.gradle.org/6.0/userguide/java_plugin.html
    // java
    kotlin("jvm") version "1.5.30"
    kotlin("plugin.spring") version "1.5.30"
    id("com.gorylenko.gradle-git-properties") version "2.3.1"
    // 解决kotlin无法识别java使用lombok的@getter等方法 https://kotlinlang.org/docs/lombok.html#gradle
    kotlin("plugin.lombok") version "1.5.30"
    `java-library`
    `maven-publish`
    signing
}

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

}

subprojects {
    apply {
        // plugin("io.spring.dependency-management")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("java-library")
        plugin("org.jetbrains.kotlin.plugin.lombok")
    }

    group = "com.iminling"
    version = "2.2.2-SNAPSHOT"

    dependencies {
        api(platform("org.springframework.boot:spring-boot-dependencies:2.3.5.RELEASE"))
        api(platform("org.springframework.cloud:spring-cloud-dependencies:Hoxton.SR9"))
        api(platform("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2.1.0.RELEASE"))
        api(platform("org.jetbrains.kotlin:kotlin-bom"))
        compileOnly("org.projectlombok:lombok:1.16.20")
        annotationProcessor("org.projectlombok:lombok:1.16.20")
        testCompileOnly("org.projectlombok:lombok:1.16.20")
        testAnnotationProcessor("org.projectlombok:lombok:1.16.20")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.jar {
        dependsOn(tasks.withType(GenerateMavenPom::class))
        manifest {
            attributes("Created-By" to "Gradle 7.1")
        }
        into("META-INF") {
            from("$buildDir/publications/mavenJava")
            exclude("*.asc")
            rename { it.replace("pom-default.xml", "pom.xml") }
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }
}

subprojects {

    apply{
        plugin("com.gorylenko.gradle-git-properties")
    }

    gitProperties {
        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        configure<com.gorylenko.GitPropertiesPluginExtension> {
            dateFormat = "yyyy-MM-dd HH:mm:ss"
            dateFormatTimeZone = "Asia/Shanghai"
            customProperties = mapOf("git.build.time" to time)
        }
    }

}


subprojects {
    apply{
        plugin("maven-publish")
        plugin("signing")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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

        signing {
            sign(publishing.publications["mavenJava"])
        }

        tasks.javadoc {
            if (JavaVersion.current().isJava9Compatible) {
                (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
            }
            (options as StandardJavadocDocletOptions).encoding("UTF-8")
        }
    }

    // 解决打包后生成.module文件，导致发布到中央仓库时只上传了.module文件没有上传jar文件
    tasks.withType<GenerateModuleMetadata> {
        enabled = false
    }
}