esbuild-java
============

This is a small warpper around the esbuild executable, so that you can invoke it from java.

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
        .withEntry(entry).withType(type).build();
final Path path = Bundler.bundle(bundleOptions);
```

Dependencies are a list of either webjar or mvnpm bundles, type is the type of the bundles and entry is the javascript.
Returned is the folder that contains the result of the transformation.