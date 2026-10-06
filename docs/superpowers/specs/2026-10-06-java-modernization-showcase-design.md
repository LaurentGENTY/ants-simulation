# Ants simulation — modernisation Java + médias vitrine

- **Date** : 2026-10-06
- **Auteur** : Laurent Genty (brainstorm assisté)
- **Statut** : à valider
- **Repo** : github.com/LaurentGENTY/ants-simulation (projet ENSEIRB-MATMECA, algorithmique mobile, Genty & Chataigner)

## 1. Objectif

Relancer le projet d'école comme projet perso **vitrine**, en gardant **Java + JBotSim** comme cœur (règle commune à tous les projets relancés : la techno d'origine reste le cœur, pas de réécriture).

Livrables :

1. Le projet se build et se lance **sans IntelliJ** (`./gradlew run`).
2. La simulation est **corrigée** pour que les pistes de phéromones émergent réellement.
3. Les phéromones sont **visibles en direct** dans l'UI.
4. **Médias vitrine reproductibles** (`./gradlew capture`) : GIF + MP4 + poster PNG, intégrés au README et réutilisables sur `~/perso/portfolio`.

Critère de succès : un visiteur du README voit en < 5 s une colonie qui creuse ses galeries et des pistes vertes/bleues qui se forment entre le nid et la nourriture.

## 2. Contexte : comportement actuel (extrait du code)

Vue **en coupe souterraine** : grille 30×25 sur les 2/3 bas d'une topologie 1000×800 ; chaque case a une dureté aléatoire 1–40.

- **Reine** : sur une case creusée ; chaque tick, 1 % de chance de pondre (−1 stock, stock initial 10).
- **Fourmi** : TTL 500–1500 ticks ; se déplace case par case (8-voisinage) ; une case non creusée doit d'abord être creusée (ticks = dureté).
- **Recherche** : dépose +0,1 phéromone reine par pas ; va vers la nourriture si sentie (sensing range 40) ; sinon suit la phéromone nourriture en évitant ses 2 dernières cases ; sinon aléatoire (jamais sur un rocher).
- **Retour** : prend 1 ou 2 de nourriture, dépose +0,1 phéromone nourriture par pas, remonte la phéromone reine jusqu'au nid, livre.
- **Monde** : nourriture spawnée en profondeur (gaussienne ~80 % hauteur, 10–19 unités, TTL 1000–5000) ; rochers ~60 % hauteur, infranchissables.

## 3. Décisions prises

| # | Sujet | Décision |
|---|-------|----------|
| 1 | Techno | Java conservé, pas de réécriture web |
| 2 | Fidélité | Corriger les bugs, documentés dans le README |
| 3 | Médias | Mode capture intégré, seed fixe, rendu offscreen → ffmpeg |
| 4 | Phéromones | Surimpression 2 couleurs toujours visible, touche `P` pour masquer |
| 5 | Navigateur (CheerpJ) | Hors périmètre (spike séparé éventuel) |
| 6 | Améliorations | RNG seedé unique + petit HUD ; pas de contrôles live |
| 7 | Architecture | Renderer partagé UI/capture, pas de dépendance aux internes Swing de JBotSim |

## 4. Structure et build

1. Arborescence Gradle standard via `git mv` (historique conservé) :
   - `src/ants/**` + `src/main/java/ants/AntHillMain.java` → `src/main/java/ants/**`
   - package `main.java.ants` → `ants`
   - `src/resources/images/*` → `src/main/resources/images/*` ; chemins d'icônes `/resources/images/x.png` → `/images/x.png`
2. Gradle wrapper, Kotlin DSL : plugin `application` (`mainClass = ants.AntHillMain`), toolchain **Java 21**, dépendance `io.jbotsim:jbotsim-all:1.2.0` (vérifiée sur Maven Central), JUnit 5.
3. Commandes : `./gradlew run`, `./gradlew capture`, `./gradlew test`.
4. `.gitignore` : `build/`, `.gradle/`, `.idea/`, frames temporaires. Médias finaux commités dans `media/`.
5. `rapport_algomob_genty_chataigner.pdf` reste à la racine.

## 5. Changements de simulation

### 5.1 Bugs corrigés (7)

| # | Bug | Correctif |
|---|-----|-----------|
| B1 | Tri des voisins **croissant** (`Collections.sort` + `Double.compare`) : la fourmi suit la piste la plus faible | Tri décroissant |
| B2 | `Cell.onClock` jamais enregistré : pas d'évaporation | Un seul `ClockListener` dans `Environment` qui itère toutes les cases |
| B3 | Évaporation −1 toutes les 1000 ticks avec max = 1 : remise à zéro brutale | Évaporation multiplicative par tick (`intensité *= facteur`), mise à 0 sous 0,01 ; nourriture s'évapore 2× plus vite que reine (sujet : 1000 vs 2000) |
| B4 | `RockSpawner` jamais enregistré sur l'horloge | `tp.addClockListener(this)` |
| B5 | `increment*PheromoneIntensity` ignore l'ajout s'il dépasse le max : plafond réel 0,9 | Borner à `MAX` (`min(MAX, v + delta)`) |
| B6 | `WaypointNode.onClock` appelle `onArrival()` à chaque tick : destinations empilées en continu | `onArrival()` appelé uniquement à l'arrivée |
| B7 | `QueenNode.produceOffspring` appelle `die()` à stock 0 puis pond quand même | `return` après `die()` |

Risque connu : B6 change le rythme des fourmis (déplacement case par case réel). Si la colonie devient trop lente pour la capture, ajuster `speed` dans `SimConfig`.

### 5.2 Ajouts

- **`SimRandom`** : un unique `java.util.Random` seedé, remplaçant tous les `new Random()`. Seed via argument CLI / propriété système ; aléatoire par défaut en interactif.
- **`ColonyStats`** : compteurs nourriture livrée, fourmis vivantes (stock reine lu sur `QueenNode`).
- **`SimConfig`** : constantes nommées regroupées (facteurs d'évaporation, quantités déposées, taux de ponte/spawn, vitesse). Pas de panneau UI.

## 6. Rendu et capture

### 6.1 `SimulationRenderer`

Dessine dans un `Graphics2D` :

1. **Fond** : image de terre, cases assombries selon dureté (logique actuelle de `EnvironmentBackgroundPainter`), tunnels clairs.
2. **Phéromones** : par case, vert (nourriture) et bleu (reine), alpha = intensité × 0,6, deux calques superposés. Masquables (`P`).
3. **Sprites** (mode capture uniquement) : icônes existantes (fourmi normale / creuse / porte, reine, nourriture, rocher), orientées selon la direction.
4. **HUD** : tick, fourmis vivantes, stock reine, nourriture livrée — cadre semi-transparent en haut à gauche.

### 6.2 Mode interactif (`./gradlew run`)

`EnvironmentBackgroundPainter` délègue fond + phéromones + HUD au renderer ; JBotSim dessine les sprites comme aujourd'hui. Touche `P` via `KeyListener` sur le composant `JTopology`.

### 6.3 Mode capture (`CaptureMain`)

- Headless (`-Djava.awt.headless=true`), aucune fenêtre.
- Avance la simulation **tick par tick** de façon déterministe, sans timer.
- **À vérifier en phase plan** : que JBotSim 1.2.0 expose un pas manuel (`Topology.step()` ou équivalent) sans démarrer le timer. Sinon, petite boucle de pilotage maison appelant les `onClock` dans l'ordre JBotSim.
- Arguments : `--seed <long>`, `--ticks <n>` (défaut 3000), `--every <n>` (défaut 5 → 600 frames), `--out <dir>` (défaut `build/capture/frames`).
- Rendu 1000×800 dans un `BufferedImage`, écrit en `frame_%05d.png`.

### 6.4 Tâche Gradle `capture`

`JavaExec` (`CaptureMain`, seed figée dans `build.gradle.kts`) puis deux `Exec` ffmpeg :

- `media/colony.mp4` : H.264, 30 fps, pleine taille.
- `media/colony.gif` : 640 px de large, 15 fps, `palettegen`/`paletteuse`, objectif < 8 Mo.
- `media/colony-poster.png` : dernière frame.

Choix de la seed : essayer plusieurs seeds, garder celle qui montre la plus nette formation de pistes.

## 7. Gestion des erreurs

- `ffmpeg` absent : la tâche `capture` échoue avec un message explicite (`brew install ffmpeg`) ; les PNG sont conservés.
- Argument CLI invalide dans `CaptureMain` : usage affiché, exit code 1.

## 8. Tests (JUnit 5, logique pure)

1. `Cell` : dépôt borné à 1 ; évaporation multiplicative ; mise à 0 sous 0,01 ; nourriture s'évapore 2× plus vite que reine.
2. Tri des voisins : la case la plus intense en premier.
3. `SimRandom` : même seed → même séquence.
4. Smoke test : capture headless 50 ticks, `--every 5`, seed fixe → 10 PNG non vides.

Pas de tests Swing.

## 9. README

Réécrit en anglais : GIF en tête, pitch 2 lignes, commandes `run` / `capture` / `test`, règles de la simulation, section *Improvements since the 2020 school version* (B1–B7), lien vers le rapport PDF, crédits Genty & Chataigner.

## 10. Hors périmètre

- Version navigateur (CheerpJ) — spike séparé éventuel.
- Contrôles live (vitesse, pause, clic pour poser de la nourriture).
- Réécriture web.
- Intégration dans `~/perso/portfolio` (on fournit seulement les fichiers de `media/`).

## 11. Estimation

1,5 à 2 soirées : build + déplacement 1 h · bugs + tests 2 h · renderer + HUD 1 h 30 · capture + ffmpeg 1 h 30 · README + choix de seed 1 h.
