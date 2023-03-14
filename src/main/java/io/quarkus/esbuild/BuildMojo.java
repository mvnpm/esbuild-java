package io.quarkus.esbuild;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.file.Path;

@Mojo(name = "esbuild", defaultPhase = LifecyclePhase.COMPILE)
public class BuildMojo extends AbstractMojo {

    @Parameter(property = "version", defaultValue = "0.17.10")
    private String version;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            final Path esBuildExec = new Download(version).execute();
            new Execute(esBuildExec.toFile(), new ConfigBuilder().version(true).build()).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
