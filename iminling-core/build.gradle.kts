dependencies {
    api(project(":iminling-common"))
    compileOnly("com.baomidou:mybatis-plus-boot-starter:3.4.0")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly("com.querydsl:querydsl-jpa")
    // annotationProcessor("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.hibernate.validator:hibernate-validator:6.1.6.Final")
    api("org.aspectj:aspectjweaver:1.9.0")
    api("cglib:cglib:3.1")
    api("com.auth0:java-jwt:3.3.0")
}