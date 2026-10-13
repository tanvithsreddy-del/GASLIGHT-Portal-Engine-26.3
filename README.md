# 26.3 portal engine source experiment

This repository tracks the Apache-2.0
[SeamlessPortals source](https://github.com/fmpapierz/SeamlessPortals) at
commit 0de56eb0b76690c8584b538d28f6b0143cad45f7. It is a source
foundation under evaluation for [GASLIGHT Edition](https://github.com/tanvithsreddy-del/GASLIGHT-Edition).
It is **not** the complete GASLIGHT gameplay mod or a release artifact.
The custom GASLIGHT classes present in the supplied binary are not copied
into this source repository.

The local changes pin Gradle 9.7.0 with its SHA-256, use Java 25 without a
machine-only JDK path, and apply the particle accessor mixin in the explicit
entity-portals-off configuration. GASLIGHT-facing defaults keep ordinary
Nether and End portals in the default `entityPortals=true` mode: even an older
`immersive_portals.json` requesting converted portals is guarded by
`preserveVanillaDimensionPortals=true`. The legacy `entityPortals=false`
block-era fallback still changes vanilla portals and is not a supported
GASLIGHT profile.
The first-launch Immersive Portals screen, creative-mode wiki chat tip, and
automatic update announcement are removed. Compatibility and error warnings
remain visible. The converted-portal crossing fixture opts out of the
vanilla guard only in its disposable test run.

On Windows 11 with Eclipse Adoptium JDK 25.0.4.1, Minecraft 26.3, Fabric
Loader 0.19.5 and Fabric API 0.161.0+26.3:

    .\gradlew.bat :common:test :fabric:build --no-daemon --max-workers=2
    .\gradlew.bat :fabric:runServer --no-daemon --max-workers=2
    .\gradlew.bat :fabric:runVanillaPortalGametest -PsodiumRuntime=true --no-daemon --max-workers=2
    .\gradlew.bat :fabric:runCrossingGametest -PsodiumRuntime=true --no-daemon --max-workers=2

The compile/tests and disposable dedicated-server startup/reopen/shutdown
passed. A real graphical client with Sodium passed the vanilla Nether and End
formation and travel smoke, including an old config requesting portal
conversion. The broader crossing suite reached multiple genuine gameplay
legs but **failed** its seam-mesh recompilation assertion. Earlier vanilla
renderer attempts also suffered a Windows native access violation. The full
raw evidence and limits are in the [GASLIGHT evaluation](https://github.com/tanvithsreddy-del/GASLIGHT-Edition/blob/main/docs/ENGINE_26_3_EVALUATION.md).

The [development JAR](artifacts/development/seamlessportals-fabric-26.3-vanilla-portals-dev.jar)
is the genuinely built portal-engine fork (SHA-256
`bf1fa5ca9982e5bd962ea81c3c02259c1e6ae124b5e4a084c6317dde09a01311`).
It is not a GASLIGHT gameplay release; use disposable worlds only.

Fabric, NeoForge and opt-in Forge modules are present in the source.
Only the stated Fabric build/server profile was run here. Loader presence
in a repository is not compatibility proof. Do not use this experiment
on a valuable Minecraft world.

The upstream LICENSE remains Apache-2.0. Preserve the existing source
history and notices when reusing or distributing this engine.
