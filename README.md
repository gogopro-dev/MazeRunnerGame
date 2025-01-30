# MazeRunner

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and a main class extending `Game` that sets the first screen.

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




# CLASS HIERARCHY

## HUD
`HUD` is the main class that handles the HUD of the game. It manages all thw widget subclasses each dispaying a certain part of HUD. It also interacts with outer classes such as Player and Collectable in order to update the HUD widgets.
- health `HpBar`
- stamina `StaminaBar`
- time spent in a run and score `TimeAndScore`
- collected keys and coins `Inventory`
- direction arrow that points to the exit from the labyrinth `ExitArrow` 
- inventory of the player `Inventory`
- descriptions of items on floor near player `Description`
- slots for active items `AbilityBorder`
- methods that update the HUD widgets positions and draw them on the screen in `HUD`
