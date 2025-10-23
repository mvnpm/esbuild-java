plugins {
    java
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
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.0")
    testImplementation("io.mvnpm:esbuild-java-testing:${esbuildJavaVersion}")
    testImplementation("io.mvnpm:esbuild-java:${esbuildJavaVersion}")
}

tasks.test {
    useJUnitPlatform() // or useTestNG() if relevant
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

