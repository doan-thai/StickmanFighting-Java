# stickman-fighting

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an `ApplicationAdapter` extension that draws libGDX logo.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `lwjgl3:runUiQa`: starts the app in SettingScreen UI QA mode (layout checks + auto screenshot).
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

## Automated UI QA (SettingScreen)

Use this mode when tuning UI quickly and safely. It will auto-open the settings screen,
run layout assertions, save a screenshot, then exit.

Command:

```
./gradlew lwjgl3:runUiQa
```

On Windows:

```
.\gradlew.bat lwjgl3:runUiQa
```

Output:

- Screenshot files are written to `assets/qa-artifacts/`.
- Layout warnings (if any) are logged with the `UI-QA` tag.

Optional flags (can be passed as JVM system properties or env vars):

- `ui.qa` / `UI_QA`
- `ui.qa.openSettings` / `UI_QA_OPEN_SETTINGS`
- `ui.qa.checkLayout` / `UI_QA_CHECK_LAYOUT`
- `ui.qa.captureScreenshot` / `UI_QA_CAPTURE_SCREENSHOT`
- `ui.qa.exitAfterShot` / `UI_QA_EXIT_AFTER_SHOT`
- `ui.qa.screenshotDelayFrames` / `UI_QA_SCREENSHOT_DELAY_FRAMES`
- `ui.qa.outputDir` / `UI_QA_OUTPUT_DIR`
