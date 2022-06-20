# Telegram Bot Tutorial

## Introduction before introduction :)

This project uses [Gradle](https://gradle.org) build tool and there is high chance to see gradle in the other kotlin configuration.
Each part will be included in separated gradle module as [Plugin](https://github.com/InsanusMokrassar/PlaguBot/blob/master/plugin/src/main/kotlin/dev/inmo/plagubot/Plugin.kt) from [PlaguBot](https://github.com/InsanusMokrassar/PlaguBot).
Of course, that does not mean that all the logic of each plugin will be only in one file, but I will try hard to not forget mention it.

Basically, PlaguBot uses [this template of config](https://github.com/InsanusMokrassar/PlaguBot/blob/master/template.config.json), but in each (or almost each) part of this tutorial will be shown how to add your own fields to this config and deserialize it.
There are several important things about plugins:

* Plugin realization (excepting abstract ones, of course) must have empty constructor for creating an instance or be an object (like `object MyPlugin : Plugin`)
* Plugin have two sections - for `DI` setup (lets name it config stage) and bot setup (plelaunch stage)
* Plugin have access predefined things from `koin` in `setupBotPlugin` or received in modules scopes:
  * `PlaguBot` config
  * `PlaguBot` plugins
  * Database and its config
  * Default `Json` format (can be accessed as `koin.get<Json>()`)
  * The `PlaguBot` itself

## How to launch

First, you need to change bot token inside of [config.json](config.json). The other parts should be described in the readmes of the modules with these parts.

There are two main ways to launch it:

* Run `./gradlew build && ./gradlew run --args="PATH_TO_YOUR_CONFIG"` with replacing of `PATH_TO_YOUR_CONFIG`
* Run `./gradlew build` and get [zip of bot](build/distributions/bot.zip) and unarchive it somewhere you need. In this
archive there is an executable files `bot.bat` (for windows) and `bot` (for linux) by the path inside of archive
`/bot/bin`. After unarchiving you can just launch executable file with one argument: path to the config
