plugins {
    java
    id("io.mvnpm.gradle.plugin.native-java-plugin") version "1.1.2"
}

group = "com.example"
version = "0.0"


repositories {

    mavenCentral()

    mavenLocal() {
        metadataSources {
            gradleMetadata()
            mavenPom()
        }
    }

}

val esbuildJavaVersion = project.findProperty("esbuildJavaVersion") as String

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
    testImplementation("io.mvnpm:esbuild-java-testing:${esbuildJavaVersion}")
    implementation("io.mvnpm:esbuild-java:${esbuildJavaVersion}")
}

tasks.test {
    useJUnitPlatform() // or useTestNG() if relevant
}


