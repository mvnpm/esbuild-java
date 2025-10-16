package io.mvnpm.esbuild;

import java.io.IOException;
import java.net.URISyntaxException;

import io.mvnpm.esbuild.install.EsBuildDeps;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException {

        System.out.println(EsBuildDeps.get());
    }
}
