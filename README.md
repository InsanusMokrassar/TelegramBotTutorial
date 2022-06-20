# Telegram Bot Tutorial

This tutorial aimed to show how you may break your bot parts into different modules and run it easily change the 

## How to launch

First, you need to change bot token inside of [config.json](config.json). The other parts should be described in the readmes of the modules with these parts.

There are two main ways to launch it:

* Run `./gradlew build && ./gradlew run --args="PATH_TO_YOUR_CONFIG"` with replacing of `PATH_TO_YOUR_CONFIG`
* Run `./gradlew build` and get [zip of bot](build/distributions/bot.zip) and unarchive it somewhere you need. In this
archive there is an executable files `bot.bat` (for windows) and `bot` (for linux) by the path inside of archive
`/bot/bin`. After unarchiving you can just launch executable file with one argument: path to the config
