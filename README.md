esbuild-java
============

[![Build Status](https://img.shields.io/github/actions/workflow/status/mvnpm/esbuild-java/maven.yml?label=Build&branch=main)](https://github.com/mvnpm/esbuild-java/actions/workflows/maven.yml)
[![usefulness 100%](https://img.shields.io/badge/usefulness-100%25-success.svg?label=Usefulness)](https://www.google.com/search?q=pasta+machine)
[![Maven Central](https://img.shields.io/maven-central/v/io.mvnpm/esbuild-java.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.mvnpm/esbuild-java)
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)


This library provides a **Java binding for [Esbuild](https://esbuild.github.io/)**, allowing you to invoke Esbuild directly from Java.  
It uses [Deno](https://deno.com/) under the hood to run Esbuild and its plugins.

Originally created for [Quarkus](https://quarkus.io/) via [quarkiverse/quarkus-web-bundler](https://github.com/quarkiverse/quarkus-web-bundler),  
it can also be used standalone or through Maven with the [mvnpm/esbuild-maven-plugin](https://github.com/mvnpm/esbuild-maven-plugin).

### Key Features
- Native Java API for Esbuild
- Supports plugins via Deno
- Works with Quarkus or standalone
- Maven integration available

---

## Quick Example

```java
public class Main {
    public static void main(String[] args) throws IOException {
        String entrypoint = args.length > 0 ? args[0] : "example.js";
        System.out.println("Using entrypoint: " + entrypoint);

        BundleOptions options = BundleOptions.builder()
                .addEntryPoint(Path.of(entrypoint))
                .build();

        BundleResult result = Bundler.bundle(options, true);
        System.out.println(result.logs());
        System.out.println("Bundling output: " + result.dist());
    }
}
```

---

## Bundling with Dependencies

You can also bundle JavaScript files that depend on **WebJars** or **mvnpm** packages:

```java
BundleOptions options = BundleOptions.builder()
        .withDependencies(dependencies) // List of WebJar or mvnpm JARs
        .addEntryPoint(Path.of(entrypoint))
        .build();

BundleResult result = Bundler.bundle(options, true);
```

---


