esbuild-java
============

[![Build Status](https://img.shields.io/github/actions/workflow/status/mvnpm/esbuild-java/maven.yml?label=Build&branch=main)](https://github.com/mvnpm/esbuild-java/actions/workflows/maven.yml)
[![usefulness 100%](https://img.shields.io/badge/usefulness-100%25-success.svg?label=Usefulness)](https://www.google.com/search?q=pasta+machine)
[![Maven Central](https://img.shields.io/maven-central/v/io.mvnpm/esbuild-java.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.mvnpm/esbuild-java)
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)

This is a small wrapper around the esbuild executable, so that you can invoke it from java.

```java
// create the command line parameters 
final EsBuildConfig esBuildConfig = new EsBuildConfigBuilder().bundle().entryPoint("main.js").outDir("dist").build();

// use the resolver to get the esbuild executable
final Path esBuildExec = new ExecutableResolver().resolve("0.17.1");
// it will use a bundled version of es build or download the right version

//execute
new Execute(esBuildExec.toFile(), esBuildConfig);
```

Another option is to use `java -jar` e.g.

```bash
java -jar target/esbuild-java-*-jar-with-dependencies.jar --help
```

Additionally, it has a utility to bundle a javascript file with webjar or mvnpm dependencies:

```java
final BundleOptions bundleOptions = new BundleOptionsBuilder().withDependencies(dependencies)
        .withEntry(entry).build();
final Path path = Bundler.bundle(bundleOptions, true);
```

Dependencies are a list of either webjar or mvnpm jars.
Returned is the folder that contains the result of the transformation.
