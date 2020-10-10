# ban

![GitHub Workflow Status](https://img.shields.io/github/workflow/status/Proximyst/ban/build?style=flat-square)
[![Discord](https://img.shields.io/discord/733744919650238494?logo=discord&style=flat-square)](https://discord.gg/3kaGYqv)
[![CodeFactor](https://www.codefactor.io/repository/github/proximyst/ban/badge)](https://www.codefactor.io/repository/github/proximyst/ban)

*ban: the simple punishment suite, built for your sake*

This is a [Velocity](https://velocitypowered.com) punishment suite plugin. It is
built to be easy to use as an administrator, developer, and player.

## Usage

The plugin is currently in a usable state, but is not recommended for production
usage.

### Requirements

The following are requirements to use the plugin:

1. A permissions plugin like [LuckPerms](https://luckperms.net/).
1. An online mode server.

   No, the plugin will not support offline mode servers.
   This decision is set in stone.
1. A MariaDB instance.

### Set up

The following steps can be followed to install and set up the plugin:

1. Drag and drop the plugin into the Velocity proxy's `plugins/` directory.
1. Start the proxy.
1. Change the MariaDB instance details in `plugins/ban/config.conf`.

   TODO: Use TOML instead.
1. Restart the proxy.

The plugin will not support SQLite or H2 in the near future. It will support
PostgreSQL.

### Commands and Permissions

The following commands are currently available:

|Name    |Permission   |Description
|:------:|:------------|:----------
| `/ban` | `ban.commands.ban` | Ban the given player from the server.
| `/unban` | `ban.commands.unban` | Unban the given player from the server.
| `/kick` | `ban.commands.kick` | Kick the given player, potentially with a reason.
| `/mute` | `ban.commands.mute` | Mute the given player, potentially with a reason.
| `/unmute` | `ban.commands.unmute` | Unmute the given player.
| `/history` | `ban.commands.history` | Get the history of the given player.

The following notification permissions exist:

* `ban.notify.ban`
* `ban.notify.kick`
* `ban.notify.mute`
* `ban.notify.warn`
* `ban.notify.lockdown` - This only notifies players who are able to bypass the
                          lockdown.

Bypass permissions do not make you immune to punishments, but will make their
effects be nil. The following bypass permissions exist:

* `ban.bypass.ban`
* `ban.bypass.kick`
* `ban.bypass.mute`
* `ban.bypass.lockdown`

## Building

1. Clone the repository.
1. Run the `build` task: `./gradlew build`.
1. Get the jar file in `build/libs/`.

## Licence

This plugin is licensed under the
[GNU Affero Public Licence v3.0](./LICENCE).