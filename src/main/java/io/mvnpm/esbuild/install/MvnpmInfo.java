package io.mvnpm.esbuild.install;

import java.util.List;
import java.util.Set;

public record MvnpmInfo(Set<InstalledDependency> installed) {

    public record InstalledDependency(String id, List<String> dirs) {

    }
}
