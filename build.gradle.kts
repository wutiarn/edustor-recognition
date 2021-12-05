import org.gradle.internal.impldep.org.apache.commons.lang.SystemUtils
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.1"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.0"
    kotlin("kapt") version "1.6.0"
    kotlin("plugin.spring") version "1.6.0"
}

group = "ru.wtrn.edustor"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    flatDir {
        dir("libs")
        dir("/usr/share/java/opencv4") // OpenCV on Ubuntu
//        dir("/usr/local/Cellar/opencv/4.5.1_3/share/java/opencv4") // OpenCV on MacOS
//        dir("C:\\Program Files\\opencv\\build\\java") // OpenCV on Windows
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.2")
    implementation("com.google.zxing:javase:3.4.1")
    // SpirePDF has been downloaded to avoid using proprietary IceBlue repo
    // Source: https://repo.e-iceblue.com/nexus/content/groups/public/e-iceblue/spire.pdf.free/4.4.1/
    implementation(
        group = "",
        name = "spire.pdf.free-4.4.1"
    )
    implementation(group = "", name = "opencv-453")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<Jar>("jar") {
    // Disable plain jar
    // See https://stackoverflow.com/questions/67663728/spring-boot-2-5-0-generates-plain-jar-file-can-i-remove-it
    enabled = false
}