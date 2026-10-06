# Ants Simulation — Java Modernization + Showcase Media Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the 2020 JBotSim ant-colony school project build with Gradle, fix its simulation bugs so pheromone trails actually emerge, show pheromones live, and produce reproducible GIF/MP4/poster media for the README and portfolio.

**Architecture:** Keep Java + JBotSim 1.2.0 as the core. A `Simulation` object owns the JBotSim `Topology` and runs all per-tick world updates through **one** clock listener (JBotSim keeps listeners in a `HashMap`, so several RNG-consuming listeners would run in a non-deterministic order). A single seeded RNG (`SimRandom`) replaces every `new Random()`. A shared `SimulationRenderer` draws background, pheromones, sprites and HUD into any `Graphics2D`: the Swing window uses it as a background painter, the headless `CaptureMain` drives a `ManualClock` tick by tick and writes PNG frames that `ffmpeg` turns into media.

**Tech Stack:** Java 21 (toolchain), Gradle 9.8.0 wrapper (Kotlin DSL), `io.jbotsim:jbotsim-all:1.2.0`, JUnit 6.1.3 (BOM), ffmpeg (Homebrew, already installed at `/opt/homebrew/bin/ffmpeg`, with `libx264`).

**Spec:** `docs/superpowers/specs/2026-10-06-java-modernization-showcase-design.md`

## Global Constraints

- Java toolchain **21**; Gradle wrapper **9.8.0**, Kotlin DSL; dependency `io.jbotsim:jbotsim-all:1.2.0`; JUnit via `org.junit:junit-bom:6.1.3`.
- Core stays Java + JBotSim — no web rewrite, no CheerpJ, no live controls (speed, pause, click-to-add-food).
- All **code comments, identifiers, commit messages and README are in English**. Comment only the *why*. Existing French comments in untouched lines stay as they are.
- Commit messages end with the line `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.
- Work only on branch `claude/nice-wilbur-14bff6` (check with `git branch --show-current` before each commit). **Never push** — the user pushes after review.
- Use `git mv` for every moved file (history must be preserved).
- Tests run headless (`java.awt.headless=true`); no Swing UI tests.
- Media outputs: `media/colony.mp4` (H.264, 30 fps, full size), `media/colony.gif` (640 px wide, 15 fps, palette-optimized, **< 8 MB**), `media/colony-poster.png` (last frame).
- Capture defaults: `--ticks 3000`, `--every 5` (→ 600 frames), `--out build/capture/frames`, frame size 1000×800, files `frame_%05d.png`.
- Pheromones: food = green, queen = blue, alpha = intensity × 0.6; key `P` toggles them in the window.
- HUD (top-left, semi-transparent box): tick, ants alive, queen stock, food delivered.

## Review Focus

1. **Same seed twice → identical world and byte-identical frames.** Any extra clock listener that consumes randomness, or any leftover `new Random()`, silently breaks reproducible captures. Pinned by `SimulationTest.sameSeedGivesSameWorld` (Task 5) and `CaptureMainTest.sameSeedGivesIdenticalFrames` (Task 7).
2. **An ant boxed in by rocks and/or the grid edge must wait, not hang.** The original `pickNeighBoringCell` loops forever in that case and freezes the whole simulation thread. Pinned by `CellNeighborTest.enclosedCellHasNoWalkableNeighbor` (Task 4).
3. **The queen starves (stock 0) → she disappears, no ant is born, the HUD shows "dead", nothing throws.** Pinned by `QueenNodeTest.starvingQueenDiesWithoutOffspring` (Task 5) and `SimulationRendererTest.rendersWhenQueenIsDead` (Task 6).
4. **After the file move, every icon path resolves on the classpath** (a wrong path makes JBotSim silently fall back to its default icon). Pinned by `ResourcesTest` (Task 1).
5. **Bad capture arguments (`--every 0`, `--ticks abc`, a flag without value, an unknown flag, `--every` > `--ticks`) → usage + exit code 1, before any frame is written.** Pinned by `CaptureOptionsTest` (Task 7).

---

## File Structure (after all tasks)

```
build.gradle.kts, settings.gradle.kts, gradlew, gradlew.bat, gradle/wrapper/*   (Task 1)
.gitignore                                                                    (Task 1)
src/main/java/ants/AntHillMain.java              interactive entry point (moved T1, rewired T5/T6)
src/main/java/ants/actors/AntNode.java           ant behaviour (moved T1, edited T2–T5)
src/main/java/ants/actors/CellLocatedNode.java   (moved T1)
src/main/java/ants/actors/QueenNode.java         queen: offspring, stock, delivered counter (T1, T2, T5)
src/main/java/ants/actors/WaypointNode.java      movement along queued cells (T1, T5)
src/main/java/ants/comparators/*.java            pheromone comparators, now descending (T1, T4)
src/main/java/ants/environment/Cell.java         grid cell: cost, digging, pheromones (T1–T4)
src/main/java/ants/environment/Environment.java  grid of cells (T1, T2, T5)
src/main/java/ants/environment/FoodNode.java     (T1, T2)
src/main/java/ants/environment/FoodSpawner.java  (T1, T2, T5)
src/main/java/ants/environment/RockNode.java     (moved T1)
src/main/java/ants/environment/RockSpawner.java  (T1, T2, T5)
src/main/java/ants/sim/SimConfig.java            named tuning constants (T2)
src/main/java/ants/sim/SimRandom.java            single seeded RNG (T2)
src/main/java/ants/sim/ManualClock.java          JBotSim clock driven by explicit tick() calls (T5)
src/main/java/ants/sim/ColonyStats.java          immutable per-tick stats snapshot (T5)
src/main/java/ants/sim/Simulation.java           builds and drives the world (T5)
src/main/java/ants/ui/SimulationRenderer.java    background, pheromones, sprites, HUD (T6)
src/main/java/ants/ui/EnvironmentBackgroundPainter.java  JBotSim adapter → renderer (T1, T6)
src/main/java/ants/capture/CaptureOptions.java   CLI parsing/validation (T7)
src/main/java/ants/capture/CaptureMain.java      headless frame writer (T7)
src/main/resources/images/*                      icons + soil (moved T1)
src/test/java/ants/...                           tests (per task)
media/colony.mp4, media/colony.gif, media/colony-poster.png   (T7)
README.md                                         (T8)
```

---

### Task 1: Gradle build and standard source layout

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `.gitignore`, `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`
- Move: `src/ants/{actors,comparators,environment,ui}` → `src/main/java/ants/…`; `src/resources/images` → `src/main/resources/images`
- Modify: `src/main/java/ants/AntHillMain.java` (package line), every `.java` using `/resources/images/`
- Test: `src/test/java/ants/ResourcesTest.java`

**Interfaces:**
- Consumes: nothing.
- Produces: `./gradlew build|test|run` working; package `ants` for `AntHillMain`; icon paths `/images/<name>`.

- [ ] **Step 1: Create `settings.gradle.kts`**

```kotlin
rootProject.name = "ants-simulation"
```

- [ ] **Step 2: Create `build.gradle.kts`**

```kotlin
plugins {
    application
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("io.jbotsim:jbotsim-all:1.2.0")

    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "ants.AntHillMain"
}

tasks.test {
    useJUnitPlatform()
    systemProperty("java.awt.headless", "true")
}
```

- [ ] **Step 3: Create `.gitignore`**

```
.gradle/
build/
.idea/
*.iml
out/
.DS_Store
```

- [ ] **Step 4: Bootstrap the Gradle wrapper** (Gradle is not installed globally; download the official distribution into a temp dir)

```bash
GRADLE_TMP=$(mktemp -d)
curl -sfL -o "$GRADLE_TMP/gradle.zip" https://services.gradle.org/distributions/gradle-9.8.0-bin.zip
unzip -q "$GRADLE_TMP/gradle.zip" -d "$GRADLE_TMP"
"$GRADLE_TMP/gradle-9.8.0/bin/gradle" wrapper --gradle-version 9.8.0 --distribution-type bin
./gradlew --version
```

Expected: `Gradle 9.8.0` printed; files `gradlew`, `gradlew.bat`, `gradle/wrapper/*` exist.

- [ ] **Step 5: Write the failing resource test** `src/test/java/ants/ResourcesTest.java`

```java
package ants;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResourcesTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "/images/ant.png", "/images/ant-digging.png", "/images/ant-bean.png",
            "/images/ant-queen.png", "/images/ant-worm.png", "/images/rock.png",
            "/images/ant-soil.jpg"})
    void imageIsOnClasspath(String path) {
        assertNotNull(ResourcesTest.class.getResource(path), path);
    }

    @Test
    void noLegacyResourcePathInSources() throws IOException {
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            List<Path> offenders = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> read(p).contains("/resources/images/"))
                    .toList();
            assertEquals(List.of(), offenders);
        }
    }

    private static String read(Path p) {
        try {
            return Files.readString(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
```

- [ ] **Step 6: Run it — expect failure**

Run: `./gradlew test --tests ants.ResourcesTest`
Expected: FAIL — `compileJava` errors (`package ants.environment does not exist`): Gradle only compiles `src/main/java`, and the `ants.*` packages are still under `src/ants`.

- [ ] **Step 7: Move sources and resources with `git mv`**

```bash
for d in actors comparators environment ui; do git mv "src/ants/$d" "src/main/java/ants/$d"; done
mkdir -p src/main/resources
git mv src/resources/images src/main/resources/images
sed -i '' 's/^package main\.java\.ants;/package ants;/' src/main/java/ants/AntHillMain.java
grep -rl '/resources/images/' src/main/java | xargs sed -i '' 's|/resources/images/|/images/|g'
ls src   # expected: only "main" and "test"
```

- [ ] **Step 8: Run tests and build — expect pass**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL`, `ResourcesTest` 8 tests passed.

- [ ] **Step 9: Smoke-run the window for 10 seconds**

```bash
(./gradlew run > build/run.log 2>&1 &) ; sleep 15; pkill -f 'ants.AntHillMain'; grep -c "Unable to set icon" build/run.log || true
```

Expected: count `0` (every icon loaded). The window shows the soil grid and the queen.

- [ ] **Step 10: Commit**

```bash
git branch --show-current   # must print claude/nice-wilbur-14bff6
git add -A
git commit -m "build: add Gradle wrapper and standard Maven layout

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>"
```

---

### Task 2: Single seeded RNG and named config

**Files:**
- Create: `src/main/java/ants/sim/SimConfig.java`, `src/main/java/ants/sim/SimRandom.java`
- Modify: `AntNode.java`, `QueenNode.java`, `Cell.java`, `Environment.java`, `FoodNode.java`, `FoodSpawner.java`, `RockSpawner.java` (all under `src/main/java/ants/`)
- Test: `src/test/java/ants/sim/SimRandomTest.java`, `src/test/java/ants/sim/NoUnseededRandomTest.java`

**Interfaces:**
- Produces:
  - `ants.sim.SimRandom`: `static java.util.Random get()`, `static void reseed(long seed)`.
  - `ants.sim.SimConfig` constants (all `public static final`): `TOPOLOGY_WIDTH=1000`, `TOPOLOGY_HEIGHT=800`, `GRID_COLUMNS=30`, `GRID_ROWS=25`, `INITIAL_FOOD=15`, `INITIAL_ROCKS=5`, `QUEEN_INITIAL_STOCK=10`, `PHEROMONE_DEPOSIT=0.1`, `PHEROMONE_MAX=1.0`, `PHEROMONE_FLOOR=0.01`, `FOOD_PHEROMONE_LIFETIME=1000`, `QUEEN_PHEROMONE_LIFETIME=2000`, `FOOD_EVAPORATION_FACTOR`, `QUEEN_EVAPORATION_FACTOR`, `QUEEN_SPAWN_PROBABILITY=0.01`, `FOOD_SPAWN_PROBABILITY=0.01`, `ROCK_SPAWN_PROBABILITY=0.005`, `ANT_SPEED=8`.

- [ ] **Step 1: Write the failing tests**

`src/test/java/ants/sim/SimRandomTest.java`:

```java
package ants.sim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;

class SimRandomTest {

    private static int[] draw(long seed) {
        SimRandom.reseed(seed);
        int[] values = new int[20];
        for (int i = 0; i < values.length; i++) values[i] = SimRandom.get().nextInt(1000);
        return values;
    }

    @Test
    void sameSeedGivesSameSequence() {
        assertArrayEquals(draw(42L), draw(42L));
    }

    @Test
    void differentSeedsGiveDifferentSequences() {
        assertFalse(Arrays.equals(draw(1L), draw(2L)));
    }
}
```

`src/test/java/ants/sim/NoUnseededRandomTest.java`:

```java
package ants.sim;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoUnseededRandomTest {

    // Any private Random breaks reproducible captures: everything must go through SimRandom.
    @Test
    void onlySimRandomCreatesRandom() throws IOException {
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            List<Path> offenders = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.endsWith("SimRandom.java"))
                    .filter(p -> read(p).contains("new Random("))
                    .toList();
            assertEquals(List.of(), offenders);
        }
    }

    private static String read(Path p) {
        try {
            return Files.readString(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
```

- [ ] **Step 2: Run — expect failure**

Run: `./gradlew test --tests 'ants.sim.*'`
Expected: compilation FAIL (`SimRandom` not found).

- [ ] **Step 3: Create `SimRandom` and `SimConfig`**

`src/main/java/ants/sim/SimRandom.java`:

```java
package ants.sim;

import java.util.Random;

/**
 * The one random source of the simulation. JBotSim instantiates nodes itself, so a global
 * holder is the only way to give every actor the same seeded stream.
 */
public final class SimRandom {

    private static Random random = new Random();

    private SimRandom() {
    }

    public static Random get() {
        return random;
    }

    public static void reseed(long seed) {
        random = new Random(seed);
    }
}
```

`src/main/java/ants/sim/SimConfig.java`:

```java
package ants.sim;

/** Tuning constants of the simulation, grouped so they can be adjusted in one place. */
public final class SimConfig {

    private SimConfig() {
    }

    public static final int TOPOLOGY_WIDTH = 1000;
    public static final int TOPOLOGY_HEIGHT = 800;
    public static final int GRID_COLUMNS = 30;
    public static final int GRID_ROWS = 25;

    public static final int INITIAL_FOOD = 15;
    public static final int INITIAL_ROCKS = 5;
    public static final int QUEEN_INITIAL_STOCK = 10;

    public static final double PHEROMONE_DEPOSIT = 0.1;
    public static final double PHEROMONE_MAX = 1.0;
    /** Below this intensity a pheromone is considered gone and reset to 0. */
    public static final double PHEROMONE_FLOOR = 0.01;

    /** Ticks for a saturated cell to fade to PHEROMONE_FLOOR (subject: 1000 for food, 2000 for queen). */
    public static final int FOOD_PHEROMONE_LIFETIME = 1000;
    public static final int QUEEN_PHEROMONE_LIFETIME = 2000;
    public static final double FOOD_EVAPORATION_FACTOR =
            Math.pow(PHEROMONE_FLOOR / PHEROMONE_MAX, 1.0 / FOOD_PHEROMONE_LIFETIME);
    public static final double QUEEN_EVAPORATION_FACTOR =
            Math.pow(PHEROMONE_FLOOR / PHEROMONE_MAX, 1.0 / QUEEN_PHEROMONE_LIFETIME);

    public static final double QUEEN_SPAWN_PROBABILITY = 0.01;
    public static final double FOOD_SPAWN_PROBABILITY = 0.01;
    public static final double ROCK_SPAWN_PROBABILITY = 0.005;

    public static final double ANT_SPEED = 8;
}
```

- [ ] **Step 4: Replace every `new Random()` with `SimRandom.get()`** (add `import ants.sim.SimRandom;` and remove now-unused `import java.util.Random;` in each file)

| File | Before | After |
|------|--------|-------|
| `actors/AntNode.java` | `this.TTL = new Random().nextInt(MAX_TTL) + MIN_TTL;` | `this.TTL = SimRandom.get().nextInt(MAX_TTL) + MIN_TTL;` |
| `actors/AntNode.java` (`pickNeighBoringCell`) | `Random random = new Random();` | `Random random = SimRandom.get();` (keep `import java.util.Random;` here — removed in Task 4) |
| `actors/AntNode.java` (`takeFood`) | `(new Random()).nextInt(MAX_QUANTITY) + 1` | `SimRandom.get().nextInt(MAX_QUANTITY) + 1` |
| `actors/QueenNode.java` | `return new Random().nextDouble() < 0.01;` | `return SimRandom.get().nextDouble() < SimConfig.QUEEN_SPAWN_PROBABILITY;` |
| `environment/Cell.java` | `new Random().nextInt(MAX_COST_VALUE - MIN_COST_VALUE+1)` | `SimRandom.get().nextInt(MAX_COST_VALUE - MIN_COST_VALUE+1)` |
| `environment/Environment.java` | field `private final Random locationRandom;` + `locationRandom = new Random();` | delete both; replace each `locationRandom.` with `SimRandom.get().` |
| `environment/Environment.java` (`getRandomLocationDepth`) | `Random r = new Random();` | `Random r = SimRandom.get();` (keep `import java.util.Random;`) |
| `environment/FoodNode.java` | three `new Random()` | `SimRandom.get()` each |
| `environment/FoodSpawner.java` | field `random` + `random = new Random();` | delete both; `shouldSpawn()` → `return SimRandom.get().nextDouble() < SimConfig.FOOD_SPAWN_PROBABILITY;` |
| `environment/RockSpawner.java` | field `random` + `random = new Random();` | delete both; `shouldSpawn()` → `return SimRandom.get().nextDouble() < SimConfig.ROCK_SPAWN_PROBABILITY;` |

Add `import ants.sim.SimConfig;` in `QueenNode.java`, `FoodSpawner.java` and `RockSpawner.java`.

Also in `actors/AntNode.java` delete the two `System.out.println(...)` lines in `takeFood` and `dropFood` (they flood the capture output).

- [ ] **Step 5: Run — expect pass**

Run: `./gradlew test`
Expected: `BUILD SUCCESSFUL`; `SimRandomTest` (2), `NoUnseededRandomTest` (1), `ResourcesTest` (8) pass.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "refactor: route all randomness through one seeded SimRandom

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>"
```

---

### Task 3: Pheromone deposit clamp and progressive evaporation (bugs B3, B5)

**Files:**
- Modify: `src/main/java/ants/environment/Cell.java`, `src/main/java/ants/actors/AntNode.java`
- Test: `src/test/java/ants/environment/CellPheromoneTest.java`

**Interfaces:**
- Consumes: `SimConfig.PHEROMONE_MAX`, `PHEROMONE_FLOOR`, `FOOD_EVAPORATION_FACTOR`, `QUEEN_EVAPORATION_FACTOR`, `FOOD_PHEROMONE_LIFETIME`, `PHEROMONE_DEPOSIT`.
- Produces: `Cell.evaporate()` (public, one tick of decay for both pheromones). `Cell` **no longer implements `ClockListener`** and has no `onClock()`. Existing `incrementFoodPheromoneIntensity(double)` / `incrementQueenPheromoneIntensity(double)` / getters keep their names.

- [ ] **Step 1: Write the failing test** `src/test/java/ants/environment/CellPheromoneTest.java`

```java
package ants.environment;

import ants.sim.SimConfig;
import io.jbotsim.core.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CellPheromoneTest {

    private static Cell saturatedCell() {
        Cell cell = new Cell(new Point(0, 0));
        for (int i = 0; i < 20; i++) {
            cell.incrementFoodPheromoneIntensity(SimConfig.PHEROMONE_DEPOSIT);
            cell.incrementQueenPheromoneIntensity(SimConfig.PHEROMONE_DEPOSIT);
        }
        return cell;
    }

    @Test
    void depositIsClampedToMax() {
        Cell cell = saturatedCell();
        assertEquals(SimConfig.PHEROMONE_MAX, cell.getFoodPheromoneIntensity(), 1e-9);
        assertEquals(SimConfig.PHEROMONE_MAX, cell.getQueenPheromoneIntensity(), 1e-9);
    }

    @Test
    void evaporationIsProgressive() {
        Cell cell = saturatedCell();
        cell.evaporate();
        assertEquals(SimConfig.FOOD_EVAPORATION_FACTOR, cell.getFoodPheromoneIntensity(), 1e-9);
        assertEquals(SimConfig.QUEEN_EVAPORATION_FACTOR, cell.getQueenPheromoneIntensity(), 1e-9);
    }

    @Test
    void pheromoneBelowFloorIsResetToZero() {
        Cell cell = new Cell(new Point(0, 0));
        cell.incrementFoodPheromoneIntensity(SimConfig.PHEROMONE_FLOOR * 1.0001);
        cell.evaporate();
        assertEquals(0.0, cell.getFoodPheromoneIntensity());
    }

    @Test
    void foodPheromoneFadesTwiceAsFastAsQueenPheromone() {
        Cell cell = saturatedCell();
        for (int i = 0; i <= SimConfig.FOOD_PHEROMONE_LIFETIME; i++) cell.evaporate();
        assertEquals(0.0, cell.getFoodPheromoneIntensity());
        assertTrue(cell.getQueenPheromoneIntensity() > 0.05,
                "queen pheromone should still be clearly visible, was " + cell.getQueenPheromoneIntensity());
    }
}
```

- [ ] **Step 2: Run — expect failure**

Run: `./gradlew test --tests ants.environment.CellPheromoneTest`
Expected: compilation FAIL (`evaporate()` undefined).

- [ ] **Step 3: Rewrite the pheromone part of `Cell.java`**

1. Class declaration: `public class Cell extends Point implements ClockListener {` → `public class Cell extends Point {`; delete `import io.jbotsim.core.event.ClockListener;`; add `import ants.sim.SimConfig;`.
2. Delete the fields/constants `MAX_FOOD_VALUE`, `MAX_QUEEN_VALUE`, `foodPheromoneTTL`, `FOOD_TTL`, `queenPheromoneTTL`, `QUEEN_TTL`, and the two constructor lines initialising `foodPheromoneTTL` / `queenPheromoneTTL`.
3. Replace the whole `@Override public void onClock() { … }` method with:

```java
    /** One tick of exponential decay; values under the floor are cleared so trails really end. */
    public void evaporate() {
        foodPheromoneIntensity = decay(foodPheromoneIntensity, SimConfig.FOOD_EVAPORATION_FACTOR);
        queenPheromoneIntensity = decay(queenPheromoneIntensity, SimConfig.QUEEN_EVAPORATION_FACTOR);
    }

    private static double decay(double intensity, double factor) {
        double next = intensity * factor;
        return next < SimConfig.PHEROMONE_FLOOR ? 0 : next;
    }
```

4. Replace both increment methods with:

```java
    public void incrementFoodPheromoneIntensity(double value) {
        foodPheromoneIntensity = Math.min(SimConfig.PHEROMONE_MAX, foodPheromoneIntensity + value);
    }
```

```java
    public void incrementQueenPheromoneIntensity(double value) {
        queenPheromoneIntensity = Math.min(SimConfig.PHEROMONE_MAX, queenPheromoneIntensity + value);
    }
```

- [ ] **Step 4: Use the shared deposit constant in `AntNode.java`**

Delete the constants `FOOD_PHEROMONE_QUANTITY` and `QUEEN_PHEROMONE_QUANTITY`; replace every use of either with `SimConfig.PHEROMONE_DEPOSIT`; add `import ants.sim.SimConfig;`.

- [ ] **Step 5: Run — expect pass**

Run: `./gradlew test`
Expected: `BUILD SUCCESSFUL`, `CellPheromoneTest` 4 passed, earlier tests still green.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "fix: clamp pheromone deposits and evaporate them progressively

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>"
```

---

### Task 4: Follow the strongest trail and never hang when boxed in (bug B1 + Review Focus 2)

**Files:**
- Modify: `src/main/java/ants/comparators/FoodPheromoneComparator.java`, `src/main/java/ants/comparators/QueenPheromoneComparator.java`, `src/main/java/ants/environment/Cell.java`, `src/main/java/ants/actors/AntNode.java`
- Test: `src/test/java/ants/comparators/PheromoneComparatorTest.java`, `src/test/java/ants/environment/CellNeighborTest.java`

**Interfaces:**
- Produces: `Cell randomWalkableNeighbor(java.util.Random random)` — a uniformly chosen non-null, non-rock neighbour, or `null` if none. `AntNode.pickNeighBoringCell()` is **removed**.

- [ ] **Step 1: Write the failing tests**

`src/test/java/ants/comparators/PheromoneComparatorTest.java`:

```java
package ants.comparators;

import ants.environment.Cell;
import io.jbotsim.core.Point;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

class PheromoneComparatorTest {

    private static Cell cell(double food, double queen) {
        Cell c = new Cell(new Point(0, 0));
        c.incrementFoodPheromoneIntensity(food);
        c.incrementQueenPheromoneIntensity(queen);
        return c;
    }

    @Test
    void strongestFoodTrailComesFirst() {
        Cell weak = cell(0.1, 0), strong = cell(0.8, 0), medium = cell(0.4, 0);
        List<Cell> cells = new ArrayList<>(List.of(weak, strong, medium));
        cells.sort(new FoodPheromoneComparator());
        assertSame(strong, cells.get(0));
        assertSame(weak, cells.get(2));
    }

    @Test
    void strongestQueenTrailComesFirst() {
        Cell weak = cell(0, 0.1), strong = cell(0, 0.8), medium = cell(0, 0.4);
        List<Cell> cells = new ArrayList<>(List.of(weak, strong, medium));
        cells.sort(new QueenPheromoneComparator());
        assertSame(strong, cells.get(0));
        assertSame(weak, cells.get(2));
    }
}
```

`src/test/java/ants/environment/CellNeighborTest.java`:

```java
package ants.environment;

import io.jbotsim.core.Point;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CellNeighborTest {

    private static Cell rock() {
        Cell c = new Cell(new Point(0, 0));
        c.setRock(true);
        return c;
    }

    @Test
    void enclosedCellHasNoWalkableNeighbor() {
        // Corner cell: 3 real neighbours, all rocks, 5 null slots (outside the grid).
        Cell corner = new Cell(new Point(0, 0));
        corner.setRightNeighbor(rock());
        corner.setBottomNeighbor(rock());
        corner.setBottomRightNeighbor(rock());
        assertNull(corner.randomWalkableNeighbor(new Random(1)));
    }

    @Test
    void onlyFreeNeighborIsAlwaysChosen() {
        Cell center = new Cell(new Point(0, 0));
        Cell free = new Cell(new Point(1, 0));
        for (int i = 0; i < 8; i++) center.setNeighBor(i, rock());
        center.setNeighBor(Cell.RIGHT, free);
        Random random = new Random(7);
        for (int i = 0; i < 50; i++) assertSame(free, center.randomWalkableNeighbor(random));
    }
}
```

- [ ] **Step 2: Run — expect failure**

Run: `./gradlew test --tests 'ants.comparators.*' --tests ants.environment.CellNeighborTest`
Expected: compilation FAIL (`randomWalkableNeighbor` undefined).

- [ ] **Step 3: Make both comparators descending**

`FoodPheromoneComparator.compare` body → `return Double.compare(c2.getFoodPheromoneIntensity(), c1.getFoodPheromoneIntensity());`
`QueenPheromoneComparator.compare` body → `return Double.compare(c2.getQueenPheromoneIntensity(), c1.getQueenPheromoneIntensity());`
Update each class comment to say "strongest first" (English).

- [ ] **Step 4: Add `randomWalkableNeighbor` to `Cell.java`** (after `getAllNeighbors()`; add `import java.util.Random;`)

```java
    /** Uniform pick among neighbours an ant may enter, or null when the cell is boxed in. */
    public Cell randomWalkableNeighbor(Random random) {
        ArrayList<Cell> walkable = new ArrayList<>();
        for (Cell neighbor : getAllNeighbors())
            if (!neighbor.isRock())
                walkable.add(neighbor);
        if (walkable.isEmpty())
            return null;
        return walkable.get(random.nextInt(walkable.size()));
    }
```

- [ ] **Step 5: Use it in `AntNode.java`**

Replace `pickRandomDestination()` and delete `pickNeighBoringCell()` entirely:

```java
    /* choisi une cellule aleatoire dans les 8 cases autour de celle courante */
    public void pickRandomDestination() {
        Cell cell = currentCell.randomWalkableNeighbor(SimRandom.get());
        // Boxed in by rocks or the grid edge: wait on the spot and retry on next arrival.
        addDestination(cell != null ? cell : currentCell);
    }
```

Remove `import java.util.Random;` from `AntNode.java` if no longer used.

- [ ] **Step 6: Run — expect pass**

Run: `./gradlew test`
Expected: `BUILD SUCCESSFUL`, new tests (4) pass, all earlier tests green.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "fix: follow the strongest pheromone trail and wait when boxed in

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>"
```

---

### Task 5: `Simulation`, manual clock, world tick and remaining bug fixes (B2, B4, B6, B7)

**Files:**
- Create: `src/main/java/ants/sim/ManualClock.java`, `src/main/java/ants/sim/ColonyStats.java`, `src/main/java/ants/sim/Simulation.java`
- Modify: `actors/WaypointNode.java`, `actors/QueenNode.java`, `environment/Environment.java`, `environment/FoodSpawner.java`, `environment/RockSpawner.java`, `AntHillMain.java`
- Test: `src/test/java/ants/sim/ManualClockTest.java`, `src/test/java/ants/sim/SimulationTest.java`, `src/test/java/ants/actors/QueenNodeTest.java`

**Interfaces:**
- Consumes: `SimRandom.reseed(long)`, `SimConfig.*`, `Cell.evaporate()`.
- Produces:
  - `ants.sim.ManualClock extends io.jbotsim.core.Clock`: `public ManualClock(ClockManager)`, `public void tick()`, `public static ManualClock last()`.
  - `ants.sim.ColonyStats` record: `int tick(), int ants(), int queenStock(), int foodDelivered(), boolean queenAlive()`; `static ColonyStats snapshot(Topology, QueenNode)`.
  - `ants.sim.Simulation`: `static Simulation interactive(long seed)`, `static Simulation manual(long seed)`, `void start()`, `void step()` (manual only, else `IllegalStateException`), `Topology topology()`, `Environment environment()`, `QueenNode queen()`, `ColonyStats stats()`.
  - `Environment.forEachCell(java.util.function.Consumer<Cell>)`, `Environment.evaporate()`.
  - `FoodSpawner.tick()`, `RockSpawner.tick()` (no longer `ClockListener`s).
  - `QueenNode(int initialStock)` + no-arg constructor (uses `SimConfig.QUEEN_INITIAL_STOCK`), `int getFoodStock()`, `int getFoodDelivered()`.

- [ ] **Step 1: Write the failing tests**

`src/test/java/ants/sim/ManualClockTest.java`:

```java
package ants.sim;

import io.jbotsim.core.Topology;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManualClockTest {

    @Test
    void ticksOnlyWhenAsked() {
        Topology tp = new Topology(100, 100);
        tp.setClockModel(ManualClock.class);
        int[] calls = {0};
        tp.addClockListener(() -> calls[0]++);
        tp.start();
        ManualClock clock = ManualClock.last();
        for (int i = 0; i < 10; i++) clock.tick();
        assertEquals(10, calls[0]);
    }
}
```

`src/test/java/ants/actors/QueenNodeTest.java`:

```java
package ants.actors;

import ants.sim.ManualClock;
import io.jbotsim.core.Topology;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class QueenNodeTest {

    @Test
    void starvingQueenDiesWithoutOffspring() {
        Topology tp = new Topology(100, 100);
        tp.setClockModel(ManualClock.class);
        QueenNode queen = new QueenNode(0);
        tp.addNode(50, 50, queen);
        tp.start();

        queen.produceOffspring();
        ManualClock.last().tick();

        assertFalse(tp.getNodes().contains(queen));
        assertEquals(0, tp.getNodes().stream().filter(n -> n instanceof AntNode).count());
    }

    @Test
    void deliveriesAreCounted() {
        QueenNode queen = new QueenNode(10);
        queen.increaseFoodStock(2);
        queen.increaseFoodStock(1);
        assertEquals(13, queen.getFoodStock());
        assertEquals(3, queen.getFoodDelivered());
    }
}
```

`src/test/java/ants/sim/SimulationTest.java`:

```java
package ants.sim;

import ants.environment.Cell;
import io.jbotsim.core.Node;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationTest {

    private static Simulation run(long seed, int ticks) {
        Simulation sim = Simulation.manual(seed);
        sim.start();
        for (int i = 0; i < ticks; i++) sim.step();
        return sim;
    }

    private static String fingerprint(Simulation sim) {
        StringBuilder sb = new StringBuilder(sim.stats().toString());
        sim.environment().forEachCell(c -> sb
                .append(c.isDug() ? '1' : '0')
                .append(String.format(Locale.ROOT, "%.6f/%.6f;",
                        c.getFoodPheromoneIntensity(), c.getQueenPheromoneIntensity())));
        for (Node n : sim.topology().getNodes())
            sb.append(n.getClass().getSimpleName())
              .append(String.format(Locale.ROOT, "@%.3f,%.3f;", n.getX(), n.getY()));
        return sb.toString();
    }

    @Test
    void sameSeedGivesSameWorld() {
        assertEquals(fingerprint(run(42L, 1500)), fingerprint(run(42L, 1500)));
    }

    @Test
    void coloniesGrowDigAndLayTrails() {
        Simulation sim = run(42L, 1500);
        AtomicInteger dug = new AtomicInteger();
        double[] queenPheromone = {0};
        sim.environment().forEachCell(c -> {
            if (c.isDug()) dug.incrementAndGet();
            queenPheromone[0] += c.getQueenPheromoneIntensity();
        });
        assertTrue(sim.stats().tick() > 1400, "tick was " + sim.stats().tick());
        assertTrue(dug.get() > 1, "ants should have dug tunnels, dug=" + dug.get());
        assertTrue(queenPheromone[0] > 0, "ants should have laid queen pheromone");
    }

    @Test
    void stepRequiresManualClock() {
        Simulation sim = Simulation.interactive(1L);
        assertThrows(IllegalStateException.class, sim::step);
    }
}
```

- [ ] **Step 2: Run — expect failure**

Run: `./gradlew test --tests 'ants.sim.*' --tests ants.actors.QueenNodeTest`
Expected: compilation FAIL (`ManualClock`, `Simulation`, `QueenNode(int)` undefined).

- [ ] **Step 3: Create `ManualClock`**

`src/main/java/ants/sim/ManualClock.java`:

```java
package ants.sim;

import io.jbotsim.core.Clock;
import io.jbotsim.core.ClockManager;

/**
 * JBotSim clock that advances only on explicit {@link #tick()} calls, on the caller's thread.
 * Topology.step() is not usable for captures: it resumes the timer thread asynchronously.
 */
public class ManualClock extends Clock {

    // JBotSim instantiates the clock by reflection and exposes no getter for it.
    private static volatile ManualClock last;

    private boolean running;
    private int timeUnit = 10;

    public ManualClock(ClockManager manager) {
        super(manager);
        last = this;
    }

    /** The clock created by the latest Topology.start() using this model. */
    public static ManualClock last() {
        return last;
    }

    public void tick() {
        manager.onClock();
    }

    @Override
    public int getTimeUnit() {
        return timeUnit;
    }

    @Override
    public void setTimeUnit(int timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void pause() {
        running = false;
    }

    @Override
    public void resume() {
        running = true;
    }
}
```

- [ ] **Step 4: Fix `WaypointNode.onClock` (B6)** — replace the method and use the config speed

```java
    double speed = SimConfig.ANT_SPEED;

    @Override
    public void onClock() {
        if (destinations.isEmpty())
            return;
        Point dest = destinations.peek();
        if (distance(dest) > speed) {
            setDirection(dest);
            move(speed);
        } else {
            setLocation(dest);
            destinations.poll();
            // Only on real arrival: calling it every tick re-planned and piled up destinations.
            onArrival();
        }
    }
```

Add `import ants.sim.SimConfig;` and delete the old `double speed = 8;` line.

- [ ] **Step 5: Update `QueenNode` (B7 + counters)** — full new file body

```java
package ants.actors;

import ants.sim.SimConfig;
import ants.sim.SimRandom;

public class QueenNode extends CellLocatedNode {

    /* stock de nourriture (pas de TTL, une reine fourmi peut vivre jusqua 37 ans !) */
    private int foodStock;
    private int foodDelivered;

    public QueenNode() {
        this(SimConfig.QUEEN_INITIAL_STOCK);
    }

    public QueenNode(int initialStock) {
        super();
        foodStock = initialStock;

        setIcon("/images/ant-queen.png");
        setIconSize(getIconSize() * 2);
    }

    @Override
    /* production recurrent de fourmis */
    public void onClock() {
        if (shouldProduceOffspring())
            produceOffspring();
    }

    private boolean shouldProduceOffspring() {
        return SimRandom.get().nextDouble() < SimConfig.QUEEN_SPAWN_PROBABILITY;
    }

    public void produceOffspring() {
        if (foodStock <= 0) {
            die();
            // A dead queen must not lay: the original fell through and spawned anyway.
            return;
        }
        foodStock--;

        AntNode babyAnt = new AntNode(this);
        babyAnt.setCurrentCell(getCurrentCell());
        getTopology().addNode(babyAnt);
    }

    public void increaseFoodStock(int value) {
        this.foodStock += value;
        this.foodDelivered += value;
    }

    public int getFoodStock() {
        return foodStock;
    }

    public int getFoodDelivered() {
        return foodDelivered;
    }
}
```

- [ ] **Step 6: `Environment` helpers** — add (with `import java.util.function.Consumer;`)

```java
    public void forEachCell(Consumer<Cell> action) {
        for (int x = 0; x < nbColumn; x++)
            for (int y = 0; y < nbRow; y++)
                action.accept(getElement(x, y));
    }

    public void evaporate() {
        forEachCell(Cell::evaporate);
    }
```

- [ ] **Step 7: Spawners become plain tickers (B4)**

`FoodSpawner.java`: remove `implements ClockListener`, its import, the `tp.addClockListener(this);` constructor line and `@Override`; rename `public void onClock()` → `public void tick()`.

`RockSpawner.java`: same (remove `implements ClockListener` + import + `@Override`; rename `onClock()` → `tick()`), and keep rocks off dug cells so one can never land on the queen's chamber or a tunnel:

```java
        Cell location = environment.getRandomLocationDepth(0.6,5);
        while(location.isFood() || location.isDug())
            location = environment.getRandomLocationDepth(0.6,5);
```

- [ ] **Step 8: Create `ColonyStats`**

`src/main/java/ants/sim/ColonyStats.java`:

```java
package ants.sim;

import ants.actors.AntNode;
import ants.actors.QueenNode;
import io.jbotsim.core.Node;
import io.jbotsim.core.Topology;

/** Immutable per-tick snapshot, safe to read from the Swing thread while the simulation runs. */
public record ColonyStats(int tick, int ants, int queenStock, int foodDelivered, boolean queenAlive) {

    static ColonyStats snapshot(Topology topology, QueenNode queen) {
        int ants = 0;
        for (Node node : topology.getNodes())
            if (node instanceof AntNode)
                ants++;
        boolean queenAlive = topology.getNodes().contains(queen);
        return new ColonyStats(topology.getTime(), ants, queen.getFoodStock(), queen.getFoodDelivered(), queenAlive);
    }
}
```

- [ ] **Step 9: Create `Simulation`**

`src/main/java/ants/sim/Simulation.java`:

```java
package ants.sim;

import ants.actors.AntNode;
import ants.actors.QueenNode;
import ants.environment.Cell;
import ants.environment.Environment;
import ants.environment.FoodNode;
import ants.environment.FoodSpawner;
import ants.environment.RockNode;
import ants.environment.RockSpawner;
import io.jbotsim.core.Clock;
import io.jbotsim.core.DefaultClock;
import io.jbotsim.core.Topology;

/** Builds the ant world and drives its per-tick updates. */
public final class Simulation {

    private final Topology topology;
    private final Environment environment;
    private final QueenNode queen;
    private final FoodSpawner foodSpawner;
    private final RockSpawner rockSpawner;
    private final boolean manualClock;
    private ManualClock clock;
    private volatile ColonyStats stats;

    public static Simulation interactive(long seed) {
        return new Simulation(seed, DefaultClock.class);
    }

    public static Simulation manual(long seed) {
        return new Simulation(seed, ManualClock.class);
    }

    private Simulation(long seed, Class<? extends Clock> clockModel) {
        SimRandom.reseed(seed);
        manualClock = clockModel == ManualClock.class;

        topology = new Topology(SimConfig.TOPOLOGY_WIDTH, SimConfig.TOPOLOGY_HEIGHT);
        topology.setClockModel(clockModel);
        topology.setNodeModel("ant", AntNode.class);
        topology.setNodeModel("queen", QueenNode.class);
        topology.setNodeModel("food", FoodNode.class);
        topology.setNodeModel("rock", RockNode.class);

        environment = new Environment(topology, SimConfig.GRID_COLUMNS, SimConfig.GRID_ROWS);
        queen = createQueen();
        foodSpawner = new FoodSpawner(topology, environment);
        rockSpawner = new RockSpawner(topology, environment);
        for (int i = 0; i < SimConfig.INITIAL_FOOD; i++)
            foodSpawner.spawnRandomFood();
        for (int i = 0; i < SimConfig.INITIAL_ROCKS; i++)
            rockSpawner.spawnRandomRocks();

        stats = ColonyStats.snapshot(topology, queen);
        // One listener only: JBotSim keeps listeners in a HashMap, so several RNG-consuming
        // listeners would run in an arbitrary order and break seed reproducibility.
        topology.addClockListener(this::onWorldTick);
    }

    private QueenNode createQueen() {
        QueenNode q = new QueenNode();
        Cell queenCell = environment.getRandomLocation();
        q.setCurrentCell(queenCell);
        q.setLocation(queenCell);
        queenCell.setCost(Cell.MIN_COST_VALUE);
        queenCell.setDug(true);
        topology.addNode(q);
        return q;
    }

    private void onWorldTick() {
        environment.evaporate();
        foodSpawner.tick();
        rockSpawner.tick();
        stats = ColonyStats.snapshot(topology, queen);
    }

    public void start() {
        topology.start();
        if (manualClock)
            clock = ManualClock.last();
    }

    /** Advances one tick; only for simulations built with {@link #manual(long)} and started. */
    public void step() {
        if (clock == null)
            throw new IllegalStateException("step() needs a started manual simulation");
        clock.tick();
    }

    public Topology topology() {
        return topology;
    }

    public Environment environment() {
        return environment;
    }

    public QueenNode queen() {
        return queen;
    }

    public ColonyStats stats() {
        return stats;
    }
}
```

- [ ] **Step 10: Rewire `AntHillMain.java`** — full new file (renderer comes in Task 6)

```java
package ants;

import ants.sim.Simulation;
import ants.ui.EnvironmentBackgroundPainter;
import io.jbotsim.ui.JViewer;

public class AntHillMain {

    public static void main(String[] args) {
        long seed = args.length == 2 && args[0].equals("--seed") ? Long.parseLong(args[1]) : System.nanoTime();
        // Printed so a nice-looking run can be replayed with --seed.
        System.out.println("Seed: " + seed);

        Simulation simulation = Simulation.interactive(seed);
        JViewer viewer = new JViewer(simulation.topology());
        viewer.getJTopology().setDefaultBackgroundPainter(
                new EnvironmentBackgroundPainter(simulation.topology(), simulation.environment()));
        simulation.start();
    }
}
```

- [ ] **Step 11: Run — expect pass**

Run: `./gradlew test`
Expected: `BUILD SUCCESSFUL`; `ManualClockTest` (1), `QueenNodeTest` (2), `SimulationTest` (3) pass, earlier tests green.
If `sameSeedGivesSameWorld` fails: search for a remaining randomness source (another clock listener, `Math.random`, iteration over a `HashMap`/`HashSet` of nodes) — do not loosen the test.

- [ ] **Step 12: Smoke-run the window**

```bash
(./gradlew run --args="--seed 42" > build/run.log 2>&1 &) ; sleep 25; pkill -f 'ants.AntHillMain'; grep -E "Seed:|Exception" build/run.log
```

Expected: `Seed: 42`, no `Exception`. Ants visibly step cell by cell (no jitter).

- [ ] **Step 13: Commit**

```bash
git add -A
git commit -m "fix: wire evaporation and rock spawns into one deterministic world tick

Adds Simulation and a ManualClock for headless stepping, moves ants only on
real arrival and stops a starving queen from laying.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>"
```

---

### Task 6: `SimulationRenderer` — background, pheromones, sprites, HUD, `P` toggle

**Files:**
- Create: `src/main/java/ants/ui/SimulationRenderer.java`
- Modify: `src/main/java/ants/ui/EnvironmentBackgroundPainter.java` (full rewrite), `src/main/java/ants/AntHillMain.java`
- Test: `src/test/java/ants/ui/SimulationRendererTest.java`

**Interfaces:**
- Consumes: `Simulation.topology()/environment()/stats()`, `ColonyStats` accessors, `Environment.forEachCell`, `getElementWidth()/getElementHeight()`, `Cell.getCost()`, `Cell.MIN_COST_VALUE/MAX_COST_VALUE`.
- Produces: `SimulationRenderer(Simulation)`; `void paintBackground(Graphics2D)` (soil + cells + pheromones if visible), `void paintSprites(Graphics2D)`, `void paintHud(Graphics2D)`, `BufferedImage renderFrame()` (all layers, `TYPE_INT_RGB`, 1000×800), `void togglePheromones()`, `void setPheromonesVisible(boolean)`, `boolean pheromonesVisible()`.

- [ ] **Step 1: Write the failing test** `src/test/java/ants/ui/SimulationRendererTest.java`

```java
package ants.ui;

import ants.environment.Cell;
import ants.sim.SimConfig;
import ants.sim.Simulation;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationRendererTest {

    private static Simulation startedSim() {
        Simulation sim = Simulation.manual(42L);
        sim.start();
        return sim;
    }

    private static BufferedImage background(SimulationRenderer renderer) {
        BufferedImage img = new BufferedImage(SimConfig.TOPOLOGY_WIDTH, SimConfig.TOPOLOGY_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        renderer.paintBackground(g);
        g.dispose();
        return img;
    }

    @Test
    void frameHasTopologySize() {
        BufferedImage frame = new SimulationRenderer(startedSim()).renderFrame();
        assertEquals(SimConfig.TOPOLOGY_WIDTH, frame.getWidth());
        assertEquals(SimConfig.TOPOLOGY_HEIGHT, frame.getHeight());
    }

    @Test
    void foodPheromoneTintsItsCellGreenOnlyWhenVisible() {
        Simulation sim = startedSim();
        Cell cell = sim.environment().getElement(25, 20);
        cell.incrementFoodPheromoneIntensity(SimConfig.PHEROMONE_MAX);
        int x = (int) cell.getX(), y = (int) cell.getY();

        SimulationRenderer renderer = new SimulationRenderer(sim);
        renderer.setPheromonesVisible(false);
        Color hidden = new Color(background(renderer).getRGB(x, y));
        renderer.togglePheromones();
        assertTrue(renderer.pheromonesVisible());
        Color shown = new Color(background(renderer).getRGB(x, y));

        // Compare green relative to red: the cell may be white (dug) or dark soil underneath.
        assertTrue(shown.getGreen() - shown.getRed() > hidden.getGreen() - hidden.getRed() + 40,
                "expected a green tint, hidden=" + hidden + " shown=" + shown);
    }

    @Test
    void rendersWhenQueenIsDead() {
        Simulation sim = startedSim();
        sim.queen().die();
        sim.step();
        assertFalse(sim.stats().queenAlive());
        assertDoesNotThrow(() -> new SimulationRenderer(sim).renderFrame());
    }
}
```

- [ ] **Step 2: Run — expect failure**

Run: `./gradlew test --tests ants.ui.SimulationRendererTest`
Expected: compilation FAIL (`SimulationRenderer` undefined).

- [ ] **Step 3: Create `SimulationRenderer`**

`src/main/java/ants/ui/SimulationRenderer.java`:

```java
package ants.ui;

import ants.environment.Cell;
import ants.environment.Environment;
import ants.sim.ColonyStats;
import ants.sim.SimConfig;
import ants.sim.Simulation;
import io.jbotsim.core.Node;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** Draws the whole scene; shared by the Swing window and the headless capture. */
public final class SimulationRenderer {

    private static final Color FOOD_PHEROMONE = new Color(60, 220, 90);
    private static final Color QUEEN_PHEROMONE = new Color(70, 140, 255);
    private static final double PHEROMONE_MAX_ALPHA = 0.6;
    private static final Color HUD_BACKGROUND = new Color(0, 0, 0, 160);
    private static final Font HUD_FONT = new Font(Font.MONOSPACED, Font.BOLD, 16);

    private final Simulation simulation;
    private final BufferedImage soil;
    private final Map<String, BufferedImage> icons = new HashMap<>();
    private volatile boolean pheromonesVisible = true;

    public SimulationRenderer(Simulation simulation) {
        this.simulation = simulation;
        this.soil = loadImage("/images/ant-soil.jpg");
    }

    public void togglePheromones() {
        pheromonesVisible = !pheromonesVisible;
    }

    public void setPheromonesVisible(boolean visible) {
        pheromonesVisible = visible;
    }

    public boolean pheromonesVisible() {
        return pheromonesVisible;
    }

    public BufferedImage renderFrame() {
        BufferedImage frame = new BufferedImage(SimConfig.TOPOLOGY_WIDTH, SimConfig.TOPOLOGY_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = frame.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        paintBackground(g);
        paintSprites(g);
        paintHud(g);
        g.dispose();
        return frame;
    }

    public void paintBackground(Graphics2D g) {
        g.drawImage(soil, 0, 0, null);
        Environment env = simulation.environment();
        env.forEachCell(cell -> paintCell(g, cell, env));
        if (pheromonesVisible)
            env.forEachCell(cell -> paintPheromones(g, cell, env));
    }

    private static Rectangle2D bounds(Cell cell, Environment env) {
        double w = env.getElementWidth(), h = env.getElementHeight();
        return new Rectangle2D.Double(cell.getX() - w / 2, cell.getY() - h / 2, w, h);
    }

    // Same shading as the 2020 painter: dug cells are white, harder soil is darker.
    private static void paintCell(Graphics2D g, Cell cell, Environment env) {
        double cost = cell.getCost();
        Color color = Color.WHITE;
        if (cost != Cell.MIN_COST_VALUE) {
            int alpha = Math.min(255, 10 + (int) (cost / Cell.MAX_COST_VALUE * 255));
            color = new Color(0, 0, 0, alpha);
        }
        Rectangle2D r = bounds(cell, env);
        g.setColor(color);
        g.fill(r);
        g.setColor(color.brighter());
        g.draw(r);
    }

    private static void paintPheromones(Graphics2D g, Cell cell, Environment env) {
        Rectangle2D r = bounds(cell, env);
        fillTinted(g, r, FOOD_PHEROMONE, cell.getFoodPheromoneIntensity());
        fillTinted(g, r, QUEEN_PHEROMONE, cell.getQueenPheromoneIntensity());
    }

    private static void fillTinted(Graphics2D g, Rectangle2D r, Color base, double intensity) {
        if (intensity <= 0)
            return;
        int alpha = (int) Math.round(Math.min(1.0, intensity) * PHEROMONE_MAX_ALPHA * 255);
        g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));
        g.fill(r);
    }

    // Mirrors JBotSim's JNode: icon scaled to 2*iconSize, rotated by direction + PI/2 around its centre.
    public void paintSprites(Graphics2D g) {
        for (Node node : new ArrayList<>(simulation.topology().getNodes())) {
            String icon = node.getIcon();
            if (icon == null)
                continue;
            BufferedImage image = icons.computeIfAbsent(icon, SimulationRenderer::loadImage);
            int size = 2 * node.getIconSize();
            AffineTransform saved = g.getTransform();
            g.translate(node.getX(), node.getY());
            g.rotate(node.getDirection() + Math.PI / 2);
            g.drawImage(image, -size / 2, -size / 2, size, size, null);
            g.setTransform(saved);
        }
    }

    public void paintHud(Graphics2D g) {
        ColonyStats s = simulation.stats();
        String[] lines = {
                "tick           " + s.tick(),
                "ants           " + s.ants(),
                "queen stock    " + (s.queenAlive() ? String.valueOf(s.queenStock()) : "dead"),
                "food delivered " + s.foodDelivered()
        };
        g.setFont(HUD_FONT);
        FontMetrics fm = g.getFontMetrics();
        int width = 0;
        for (String line : lines)
            width = Math.max(width, fm.stringWidth(line));
        int lineHeight = fm.getHeight();
        int x = 12, y = 12, padding = 10;
        g.setColor(HUD_BACKGROUND);
        g.fillRoundRect(x, y, width + 2 * padding, lines.length * lineHeight + 2 * padding, 12, 12);
        g.setColor(Color.WHITE);
        for (int i = 0; i < lines.length; i++)
            g.drawString(lines[i], x + padding, y + padding + fm.getAscent() + i * lineHeight);
    }

    private static BufferedImage loadImage(String path) {
        URL url = SimulationRenderer.class.getResource(path);
        if (url == null)
            throw new IllegalStateException("Missing image resource: " + path);
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read image " + path, e);
        }
    }
}
```

- [ ] **Step 4: Rewrite `EnvironmentBackgroundPainter.java`** (full file)

```java
package ants.ui;

import io.jbotsim.core.Topology;
import io.jbotsim.ui.painting.JBackgroundPainter;
import io.jbotsim.ui.painting.UIComponent;

import java.awt.Graphics2D;

/** JBotSim adapter: the window paints background and HUD, JBotSim paints the sprites on top. */
public class EnvironmentBackgroundPainter extends JBackgroundPainter {

    private final SimulationRenderer renderer;

    public EnvironmentBackgroundPainter(SimulationRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void paintBackground(UIComponent c, Topology topology) {
        Graphics2D g = (Graphics2D) c.getComponent();
        renderer.paintBackground(g);
        renderer.paintHud(g);
    }
}
```

- [ ] **Step 5: Wire renderer and `P` key in `AntHillMain.java`** — replace `main` body after the seed lines

```java
        Simulation simulation = Simulation.interactive(seed);
        SimulationRenderer renderer = new SimulationRenderer(simulation);
        JViewer viewer = new JViewer(simulation.topology());
        JTopology view = viewer.getJTopology();
        view.setDefaultBackgroundPainter(new EnvironmentBackgroundPainter(renderer));
        view.setFocusable(true);
        view.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    renderer.togglePheromones();
                    view.repaint();
                }
            }
        });
        view.requestFocusInWindow();
        simulation.start();
```

Imports: `ants.ui.SimulationRenderer`, `io.jbotsim.ui.JTopology`, `java.awt.event.KeyAdapter`, `java.awt.event.KeyEvent`.

- [ ] **Step 6: Run — expect pass**

Run: `./gradlew test`
Expected: `BUILD SUCCESSFUL`; `SimulationRendererTest` 3 passed; all earlier tests green.

- [ ] **Step 7: Visual check in the window**

```bash
(./gradlew run --args="--seed 42" > build/run.log 2>&1 &) ; sleep 40; pkill -f 'ants.AntHillMain'; grep -E "Exception" build/run.log || echo "no exceptions"
```

Expected: `no exceptions`. In the window: HUD top-left, green/blue cell tints appear along ant paths, `P` hides/shows them (click the window first so it has focus).

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: draw live pheromone overlay and colony HUD

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>"
```

---

### Task 7: Headless capture, Gradle `capture` task and the showcase media

**Files:**
- Create: `src/main/java/ants/capture/CaptureOptions.java`, `src/main/java/ants/capture/CaptureMain.java`
- Modify: `build.gradle.kts`
- Create (generated, committed): `media/colony.mp4`, `media/colony.gif`, `media/colony-poster.png`
- Test: `src/test/java/ants/capture/CaptureOptionsTest.java`, `src/test/java/ants/capture/CaptureMainTest.java`

**Interfaces:**
- Consumes: `Simulation.manual/start/step/stats`, `SimulationRenderer.renderFrame()`.
- Produces: `record CaptureOptions(long seed, int ticks, int every, Path out)` with `static CaptureOptions parse(String[] args)` (throws `IllegalArgumentException`) and `static final String USAGE`; `CaptureMain.main(String[])`, `static int capture(CaptureOptions)` → number of frames written; Gradle tasks `renderFrames` and `capture` (group `showcase`), properties `-PcaptureSeed`, `-PcaptureEvery`.

- [ ] **Step 1: Write the failing tests**

`src/test/java/ants/capture/CaptureOptionsTest.java`:

```java
package ants.capture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaptureOptionsTest {

    @Test
    void defaults() {
        CaptureOptions o = CaptureOptions.parse(new String[0]);
        assertEquals(42L, o.seed());
        assertEquals(3000, o.ticks());
        assertEquals(5, o.every());
        assertEquals(Path.of("build/capture/frames"), o.out());
    }

    @Test
    void parsesAllOptions() {
        CaptureOptions o = CaptureOptions.parse(new String[]{"--seed", "7", "--ticks", "100", "--every", "10", "--out", "/tmp/x"});
        assertEquals(new CaptureOptions(7L, 100, 10, Path.of("/tmp/x")), o);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "--every 0", "--ticks 0", "--ticks abc", "--seed 1.5",
            "--ticks", "--bogus 1", "--ticks 10 --every 20"})
    void rejectsInvalidArguments(String line) {
        assertThrows(IllegalArgumentException.class, () -> CaptureOptions.parse(line.split(" ")));
    }
}
```

`src/test/java/ants/capture/CaptureMainTest.java`:

```java
package ants.capture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaptureMainTest {

    private static List<Path> frames(Path dir) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(p -> p.getFileName().toString().matches("frame_\\d{5}\\.png")).sorted().toList();
        }
    }

    @Test
    void writesOneNonEmptyFramePerInterval(@TempDir Path dir) throws IOException {
        int written = CaptureMain.capture(new CaptureOptions(42L, 50, 5, dir));
        List<Path> frames = frames(dir);
        assertEquals(10, written);
        assertEquals(10, frames.size());
        assertEquals("frame_00000.png", frames.get(0).getFileName().toString());
        for (Path f : frames) assertTrue(Files.size(f) > 1000, f + " looks empty");
    }

    @Test
    void staleFramesAreRemoved(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("frame_00099.png"), "stale");
        CaptureMain.capture(new CaptureOptions(42L, 10, 5, dir));
        assertEquals(2, frames(dir).size());
    }

    @Test
    void sameSeedGivesIdenticalFrames(@TempDir Path a, @TempDir Path b) throws IOException {
        CaptureMain.capture(new CaptureOptions(42L, 200, 100, a));
        CaptureMain.capture(new CaptureOptions(42L, 200, 100, b));
        assertArrayEquals(Files.readAllBytes(a.resolve("frame_00001.png")), Files.readAllBytes(b.resolve("frame_00001.png")));
    }
}
```

- [ ] **Step 2: Run — expect failure**

Run: `./gradlew test --tests 'ants.capture.*'`
Expected: compilation FAIL (`CaptureOptions` undefined).

- [ ] **Step 3: Create `CaptureOptions`**

```java
package ants.capture;

import java.nio.file.Path;

/** Validated command-line options of {@link CaptureMain}. */
public record CaptureOptions(long seed, int ticks, int every, Path out) {

    public static final String USAGE =
            "Usage: CaptureMain [--seed <long>] [--ticks <n>] [--every <n>] [--out <dir>]";

    public static CaptureOptions parse(String[] args) {
        long seed = 42L;
        int ticks = 3000;
        int every = 5;
        Path out = Path.of("build/capture/frames");
        for (int i = 0; i < args.length; i += 2) {
            String flag = args[i];
            if (i + 1 >= args.length)
                throw new IllegalArgumentException("Missing value for " + flag);
            String value = args[i + 1];
            switch (flag) {
                case "--seed" -> seed = Long.parseLong(value);
                case "--ticks" -> ticks = Integer.parseInt(value);
                case "--every" -> every = Integer.parseInt(value);
                case "--out" -> out = Path.of(value);
                default -> throw new IllegalArgumentException("Unknown option: " + flag);
            }
        }
        if (ticks <= 0)
            throw new IllegalArgumentException("--ticks must be > 0");
        if (every <= 0)
            throw new IllegalArgumentException("--every must be > 0");
        if (every > ticks)
            throw new IllegalArgumentException("--every must be <= --ticks");
        return new CaptureOptions(seed, ticks, every, out);
    }
}
```

(`NumberFormatException` extends `IllegalArgumentException`, so `--ticks abc` is covered.)

- [ ] **Step 4: Create `CaptureMain`**

```java
package ants.capture;

import ants.sim.ColonyStats;
import ants.sim.Simulation;
import ants.ui.SimulationRenderer;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/** Runs the simulation headless with a fixed seed and writes one PNG every N ticks. */
public final class CaptureMain {

    private CaptureMain() {
    }

    public static void main(String[] args) throws IOException {
        CaptureOptions options;
        try {
            options = CaptureOptions.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println(CaptureOptions.USAGE);
            System.exit(1);
            return;
        }
        int frames = capture(options);
        System.out.println("Wrote " + frames + " frames to " + options.out());
    }

    public static int capture(CaptureOptions options) throws IOException {
        Files.createDirectories(options.out());
        deleteOldFrames(options.out());

        Simulation simulation = Simulation.manual(options.seed());
        simulation.start();
        SimulationRenderer renderer = new SimulationRenderer(simulation);

        int written = 0;
        for (int tick = 1; tick <= options.ticks(); tick++) {
            simulation.step();
            if (tick % options.every() == 0) {
                Path file = options.out().resolve(String.format("frame_%05d.png", written));
                ImageIO.write(renderer.renderFrame(), "png", file.toFile());
                written++;
            }
        }
        ColonyStats s = simulation.stats();
        // Printed to compare seeds when picking the showcase run.
        System.out.printf("seed=%d ants=%d delivered=%d queenAlive=%b%n",
                options.seed(), s.ants(), s.foodDelivered(), s.queenAlive());
        return written;
    }

    // A shorter re-run must not leave frames from a longer one behind (ffmpeg would include them).
    private static void deleteOldFrames(Path dir) throws IOException {
        List<Path> old;
        try (Stream<Path> files = Files.list(dir)) {
            old = files.filter(p -> p.getFileName().toString().matches("frame_\\d+\\.png")).toList();
        }
        for (Path p : old)
            Files.delete(p);
    }
}
```

- [ ] **Step 5: Run — expect pass**

Run: `./gradlew test`
Expected: `BUILD SUCCESSFUL`; `CaptureOptionsTest` (9) and `CaptureMainTest` (3) pass; all earlier tests green.

- [ ] **Step 6: Add the Gradle tasks** — append to `build.gradle.kts`

```kotlin
val captureSeed = providers.gradleProperty("captureSeed").getOrElse("42")
val captureEvery = providers.gradleProperty("captureEvery").getOrElse("5")
val framesDir = layout.buildDirectory.dir("capture/frames")

val renderFrames by tasks.registering(JavaExec::class) {
    group = "showcase"
    description = "Renders simulation frames headlessly into build/capture/frames."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "ants.capture.CaptureMain"
    jvmArgs("-Djava.awt.headless=true")
    args("--seed", captureSeed, "--ticks", "3000", "--every", captureEvery,
        "--out", framesDir.get().asFile.path)
}

tasks.register("capture") {
    group = "showcase"
    description = "Renders frames, then builds media/colony.{mp4,gif} and media/colony-poster.png with ffmpeg."
    dependsOn(renderFrames)
    doLast {
        val frames = framesDir.get().asFile
        val ffmpeg = System.getenv("PATH").orEmpty().split(File.pathSeparator)
            .map { File(it, "ffmpeg") }.firstOrNull { it.canExecute() }
            ?: throw GradleException(
                "ffmpeg not found on PATH. Install it (macOS: brew install ffmpeg) and re-run " +
                "./gradlew capture. Rendered frames are kept in $frames")
        val media = file("media").apply { mkdirs() }
        val input = File(frames, "frame_%05d.png").path

        fun run(vararg command: String) {
            val process = ProcessBuilder(*command).inheritIO().start()
            if (process.waitFor() != 0) throw GradleException("Command failed: ${command.joinToString(" ")}")
        }

        run(ffmpeg.path, "-y", "-loglevel", "error", "-framerate", "30", "-i", input,
            "-c:v", "libx264", "-pix_fmt", "yuv420p", "-crf", "20", File(media, "colony.mp4").path)
        run(ffmpeg.path, "-y", "-loglevel", "error", "-framerate", "30", "-i", input,
            "-vf", "fps=15,scale=640:-1:flags=lanczos,split[a][b];[a]palettegen=max_colors=128[p];[b][p]paletteuse=dither=bayer:bayer_scale=4",
            File(media, "colony.gif").path)

        val last = frames.listFiles()!!.filter { it.name.matches(Regex("frame_\\d{5}\\.png")) }.maxBy { it.name }
        last.copyTo(File(media, "colony-poster.png"), overwrite = true)
        logger.lifecycle("Media written to ${media.path}")
    }
}
```

- [ ] **Step 7: Check the missing-ffmpeg message** (PATH without Homebrew)

```bash
PATH=/usr/bin:/bin ./gradlew capture -PcaptureEvery=1000 2>&1 | grep -E "ffmpeg not found|frames are kept"
ls build/capture/frames | head -3
```

Expected: the `ffmpeg not found on PATH … brew install ffmpeg …` message, and frames still listed. (`/usr/bin/java` must resolve; if Gradle cannot start with this PATH, run instead `env PATH=/usr/bin:/bin:$(dirname "$(/usr/libexec/java_home -v 21)")/bin ./gradlew capture -PcaptureEvery=1000`.)

- [ ] **Step 8: Pick the showcase seed**

```bash
for s in 1 2 3 4 5 6 7 8; do ./gradlew -q renderFrames -PcaptureSeed=$s -PcaptureEvery=1000 | grep '^seed='; cp build/capture/frames/frame_00002.png "build/seed-$s.png"; done
```

Open each `build/seed-<s>.png` (end of run, tick 3000) and keep the seed with `queenAlive=true`, the most `delivered`, and the most visible green trails between nest and food.
- If **no** seed keeps the queen alive: set `QUEEN_INITIAL_STOCK = 20` in `SimConfig.java`, re-run `./gradlew test`, repeat this step.
- If ants are too slow to reach food within 3000 ticks: set `ANT_SPEED = 12` in `SimConfig.java`, re-run `./gradlew test`, repeat this step.

Then replace `getOrElse("42")` in `val captureSeed` with the chosen seed (e.g. `getOrElse("5")`).

- [ ] **Step 9: Generate the media**

```bash
./gradlew capture
ls -la media/
du -k media/colony.gif
```

Expected: `colony.mp4`, `colony.gif`, `colony-poster.png` exist; GIF under 8192 KB.
If the GIF is ≥ 8 MB, in the `capture` task change `scale=640:-1` → `scale=480:-1` (keep `fps=15`), re-run, re-check. Open the GIF and confirm trails are visible; if not, return to Step 8.

- [ ] **Step 10: Commit**

```bash
git add build.gradle.kts src/main/java/ants/capture src/test/java/ants/capture src/main/java/ants/sim/SimConfig.java media/
git commit -m "feat: add headless capture and showcase media

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>"
```

---

### Task 8: README rewrite

**Files:**
- Modify: `README.md` (full rewrite)

**Interfaces:**
- Consumes: `media/colony.gif`, `media/colony.mp4`, `media/colony-poster.png`, Gradle tasks `run`, `capture`, `test`, chosen seed from Task 7 (`captureSeed` default in `build.gradle.kts`).

- [ ] **Step 1: Write `README.md`** (replace `<SEED>` with the default value of `captureSeed` in `build.gradle.kts` — it is the only value to fill in)

````markdown
# Ant colony simulation

![Ant colony digging tunnels and laying pheromone trails](media/colony.gif)

An underground ant colony in Java on [JBotSim](https://jbotsim.io): ants dig their own tunnels, find food with pheromone trails and bring it back to the queen. Green = food pheromone, blue = queen pheromone. [Full-size video](media/colony.mp4).

School project (ENSEIRB-MATMECA, mobile algorithms) by Laurent Genty and Johan Chataigner — [subject](https://www.labri.fr/perso/acasteig/teaching/algomob/practice/?p=ants-fr), [report (FR)](rapport_algomob_genty_chataigner.pdf). Modernized in 2026.

## Run it

Requires Java 21. No IDE needed.

```bash
./gradlew run                      # interactive window, random seed (printed at start)
./gradlew run --args="--seed 42"   # replay a given seed
```

Press `P` in the window to hide/show pheromones.

## Regenerate the media

Requires `ffmpeg` (`brew install ffmpeg`).

```bash
./gradlew capture                  # seed <SEED>, 3000 ticks → media/colony.{gif,mp4}, media/colony-poster.png
./gradlew capture -PcaptureSeed=7  # try another seed
```

Frames are rendered headlessly with a manual clock, so the same seed always gives the same video.

## Tests

```bash
./gradlew test
```

## Simulation rules

- **World**: side view, 30×25 grid of soil cells with random hardness. Food appears deep down, rocks (impassable) a bit higher.
- **Queen**: sits in a dug chamber, lays an ant now and then (costs 1 food); dies when her stock is empty.
- **Ant**: moves cell by cell (8 neighbours); an undug cell must be dug first, which takes longer for harder soil.
- **Searching**: lays queen pheromone at each step, goes to food it senses, otherwise follows the strongest food pheromone, otherwise wanders.
- **Returning**: takes 1–2 food, lays food pheromone at each step and follows the strongest queen pheromone home.
- **Evaporation**: pheromones fade exponentially; food trails vanish in ~1000 ticks, queen trails in ~2000.

## Improvements since the 2020 school version

1. Ants now follow the **strongest** trail (neighbours were sorted weakest first).
2. Pheromones now **evaporate** (the cell clock was never registered).
3. Evaporation is **progressive** instead of a full reset every 1000 ticks.
4. Rocks keep **spawning** during the run (the rock spawner was never registered).
5. Pheromones can reach the **maximum** of 1 (deposits above 0.9 were dropped).
6. Ants re-plan only on **arrival** instead of every tick.
7. A starving queen **no longer lays** an ant after dying.

Also: Gradle build (no IntelliJ needed), one seeded random source, live pheromone overlay, colony HUD, ants never freeze when boxed in by rocks, headless capture.
````

- [ ] **Step 2: Check links and placeholder**

```bash
grep -n "<SEED>" README.md && echo "FILL THE SEED" || echo "ok"
for f in media/colony.gif media/colony.mp4 rapport_algomob_genty_chataigner.pdf; do test -f "$f" && echo "found $f"; done
```

Expected: `ok`, then three `found` lines.

- [ ] **Step 3: Final verification**

```bash
./gradlew clean build
git status --short
```

Expected: `BUILD SUCCESSFUL`, all tests green; only `README.md` modified.

- [ ] **Step 4: Commit**

```bash
git add README.md
git commit -m "docs: rewrite README with showcase media and changelog

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>"
```
