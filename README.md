# 26.3 portal engine source experiment

This repository tracks the Apache-2.0
[SeamlessPortals source](https://github.com/fmpapierz/SeamlessPortals) at
commit 0de56eb0b76690c8584b538d28f6b0143cad45f7. It is a source
foundation under evaluation for [GASLIGHT Edition](https://github.com/tanvithsreddy-del/GASLIGHT-Edition).
It is **not** the complete GASLIGHT gameplay mod or a release artifact.
The custom GASLIGHT classes present in the supplied binary are not copied
into this source repository.

The local change pins Gradle 9.7.0 with its SHA-256, removes a machine-only
Azul JDK path while retaining Java 25, and applies the particle accessor
mixin in the explicit entity-portals-off configuration. That accessor fix
addresses a reproduced first-particle Java crash; the post-fix graphical
run is still blocked by a separate native access violation.

On Windows 11 with Eclipse Adoptium JDK 25.0.4.1, Minecraft 26.3, Fabric
Loader 0.19.5 and Fabric API 0.161.0+26.3:

    .\gradlew.bat :common:test :fabric:build --no-daemon --max-workers=2
    .\gradlew.bat :fabric:runServer --no-daemon --max-workers=2
    .\gradlew.bat :fabric:runCrossingGametest --no-daemon --max-workers=2

The compile/test and disposable server startup/shutdown passed. The
graphical crossing GameTest failed with Windows native access violation
0xC0000005 before its crossing assertions. An explicit entity-portals-off
title-card GameTest also crashed. The full raw evidence and limitations
are in the [GASLIGHT evaluation](https://github.com/tanvithsreddy-del/GASLIGHT-Edition/blob/main/docs/ENGINE_26_3_EVALUATION.md).

Fabric, NeoForge and opt-in Forge modules are present in the source.
Only the stated Fabric build/server profile was run here. Loader presence
in a repository is not compatibility proof. Do not use this experiment
on a valuable Minecraft world.

The upstream LICENSE remains Apache-2.0. Preserve the existing source
history and notices when reusing or distributing this engine.
