# MazeRunner

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and a main class extending `Game` that sets the first screen.

# How to run

## Windows

(Intellij)
Just run DesktopLauncher in the desktop project, no additional configuration is needed

## MacOS

(Intellij)
Just run DesktopLauncher with -XstartOnFirstThread jvm arguments

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.
- `android`: Android mobile platform. Needs Android SDK.
- `ios-moe`: iOS mobile backend using Multi-OS Engine.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `android:lint`: performs Android project validation.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `desktop:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `desktop:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

## Game mechanics

- The player has to find the key and bring it to the exit of the labyrinth, on his way he fights with monsters and collects items to power up.

## Controls

- `WASD` (and arrows) - movement
- `SHIFT` - sprint
- `Q` - use active Item
- `SPACE` - buy items from the shop
- `ENTER` - attack
- `ESC` - pause
- `R` - take / put away torch


# LORE
Being a hatchet man for Italian mafia isn't the most pleasant job, nevertheless, it pays your bills (and not just them) and you earn extra for staying loyal. However, even the hardest workers can fall from grace. Don Blues V Acello, his most frequent employer, was quite surprised at the productivity of his freelance worker. However, attempts to lure our hero into the mafia were unsuccessful: too much responsibility and not everyone would like to get a tight collar around his neck. Soon our hero gained fame, he assassinated many people under the leadership of Mr. Acello; and with every body his skills evolved. With each completed order, and latter refusal to join Don's mafia, the boss developed a previously unknown feeling - anxiety. What would happen, if his competitors would have found out that his most successful assassin was off-leash. It was decided: during a mission to kill Don's long-time partner (Robin Banks that is), it was our merc's death. A well-hidden trapdoor by the mansion's entrance, right at stone floor determined our killer's fate. There was a desolate, abandoned dungeon underneath, and many men have gone missing there. There is a belief that there are mysterious forces sealed down there and no one dares no more to go down there.

# PROJECT HIERARCHY
Our project is structured that way, so that in different packages there are classes that are responsible for different parts of the game. E.g. `menu` directory contains classes that are responsible for the menu of the game, `level` directory is responsible for the level in the game and so on.

## Menu
`LoadMenu` is the main class on the startup of the game. It loads all the assets into the `AssetManager`, loads all the saves (game saves, as well as settings properties) if they exist and uses `Menu` to continue with rendering.

`Menu` is the class that handles the state of the game. It manages all the menu's possible screens (e.g. `MainMenuScreen`, `SettingsScreen`, etc.) as well as the actual screen of the level (`LevelScreen`). Every time the screen is changed, the `Menu` class toggles the `MenuState` which allows to properly render the screen that should be rendered according to `MenuState`. 
## Save Management

In our project we fully implemented saving/loading mechanic. This is related to the saves of the level, and the
configurations of the settings. All saves Are stored in '.json' files (and also .png file for the minimap of the level,
which can be seen in the `PlayGameScreen` when loading a level). The saves are stored in the `saves` folder, and if the
folder does not exist, it will automatically generate itself.

## Configs
All the properties of collectables, enemies, traps, active items, and loot containers are stored in the `configs` folder. The properties are stored in '.json' files, which allow to easily change the properties.

`Globals` is a class located in the core of the project, which also have some additional properties that are used in the project. The most important properties here are `DEBUG` and `FULLBRIGHT`: First one is used to debug the game, if set to true, the level will be in debug mode which draws all the hitboxes, generated paths, all Box2d AABB queries etc. If `FULLBRIGHT` is set to true, then the whole map will be in fullbright mode (However, you will still get damage if you are technically standing in the shadow!)

## HUD

`HUD` is the main class that handles the HUD of the game. It manages all thw widget subclasses each dispaying a certain part of HUD. It also interacts with classes such as Player and Collectable in order to update the HUD widgets: `HpBar`, `StaminaBar`, `TimeAndScore`, `Inventory`, `ExitArrow`, `Description`, `AbilityBorder`.
- health `HpBar`
- stamina `StaminaBar`
- time spent in a run and score `TimeAndScore`
- collected keys and coins `Inventory`
- direction arrow that points to the exit from the labyrinth `ExitArrow` 
- inventory of the player `Inventory`
- slots for active items `AbilityBorder`
- methods that update the HUD widgets positions and draw them on the screen in `HUD`
- way of telling player any useful information `Description`

### Description
it shows information about:
- the item in near vicinity of the player
- the description of items in shop
- messages to the player about the game state

## Entities and TileEntities concept

There are two general types of dynamic objects `Entity` and `TileEntity` (somewhat like in minecraft)
The first one generally is a character, right now just `Enemy` and `Player`, that can move on its own (controlled / AI)
The way we store various Entities and TileEntities is in .json files in `assets/configs`.
They are related to the corresponding dataclass, which is normally an inner class of those, unless it was too chunky
to include in the same file. They also normally have an Enum that lists all the types of the given class, so that you
can dynamically update the generator methods / literally anything generalized.

## MazeGenerator

The `MazeGenerator` class is responsible for generating the maze (labyrinth) structure for the game. It creates the
layout of the dungeon, including walls, paths, rooms, and special points of interest such as the entrance, exit, and
collectible item placements. This class ensures the maze is procedurally generated to offer a unique experience for each
game session.

### Responsibilities:

- Implementing a procedural generation algorithm to create a maze.
- Defining boundaries, walls, open spaces (paths), and rooms in the maze.
- Placing key points such as:
    - The starting point (entrance).
    - The exit.
    - Player spawn location.
    - Locations for enemies and `Collectable` objects.
- Ensuring that the maze is solvable (using flood fill with DSU):
    - The path from the entrance to the exit should always exist.
    - Dead ends are optional but should not trap critical game items.

## Collectable

`Collectable` is the class that handles the items that can be picked up by Player. It inherits from `TileEntity`.
it has the following subclasses:
- `CollectableAttributes` - enum that stores the attributes of the collectable items

## Configs

In `assets/configs` you can change some attributes (Enemy health, enemy damage) and so on, if ever desired.
