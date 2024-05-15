package io.mvnpm.esbuild;

import java.util.Optional;

public interface BuildEventListener {

    void onBuild(Optional<BundleException> error);

}
