<!--suppress ALL -->
<p align="center">
    <img src="images/banner.png" alt="HuskTowns" />
    <a href="https://github.com/WiIIiam278/HuskTowns/actions/workflows/ci.yml">
        <img src="https://img.shields.io/github/actions/workflow/status/WiIIiam278/HuskTowns/ci.yml?branch=master&logo=github"/>
    </a> 
    <a href="https://repo.william278.net/#/releases/net/william278/husktowns/">
        <img src="https://repo.william278.net/api/badge/latest/releases/net/william278/husktowns/husktowns-common?color=00fb9a&name=Maven&prefix=v" />
    </a> 
    <a href="https://discord.gg/tVYhJfyDWG">
        <img src="https://img.shields.io/discord/818135932103557162.svg?label=&logo=discord&logoColor=fff&color=7389D8&labelColor=6A7EC2" />
    </a> 
    <br/>
    <b>
        <a href="https://www.spigotmc.org/resources/husktowns.92672/">Spigot</a>
    </b> —
    <b>
        <a href="https://william278.net/docs/husktowns/setup">Setup</a>
    </b> — 
    <b>
        <a href="https://william278.net/docs/husktowns/">Docs</a>
    </b> — 
    <b>
        <a href="http://github.com/WiIIiam278/HuskTowns/issues">Issues</a>
    </b>
</p>
<br/>

**HuskTowns** is a simple and elegant proxy-compatible Towny-style protection plugin for Spigot-based Minecraft servers. Let players form towns, claim chunks and carve out a thriving community. Built into HuskTowns is a robust and beautiful chat interface with a plethora of admin tools and config options to let you tailor the plugin to your needs.

All of this is supported on both standalone setups and across a network of proxied servers&mdash;with support for a good range of add-ons to further enhance your experience.

## Features
**⭐ Works cross-server** &mdash; Let players seamlessly manage their towns, claims and teleport to their town spawn across your proxy network!

**⭐ Super intuitive** &mdash; Users will pick up how to use it right away! Make a town with /town create and claim a chunk with /town claim to get started.

**⭐ Quick and beautiful menus** &mdash; Sometimes, simple is better. No monolithic chest GUIs—instead, robust and beautiful interactive chat menus.

**⭐ Great admin features** &mdash; Manage the towns on your server and easily make admin claims. Comes with built-in support for Dynmap, BlueMap, Pl3xMap, Plan, LuckPerms, HuskHomes & more!

**⭐ Easy to configure** &mdash; Players can fine-tune town flags and access settings in-game, and admins can easily configure roles and levels through a simple config structure.

**⭐ Extensible API & open-source** &mdash; Need more? Extend the plugin with the Developer API. Or, submit a pull request through our code bounty system!

**Ready?** [Let's head down town!](https://william278.net/docs/husktowns/setup)

## Setup
Requires Java 17+ and a Minecraft 1.17.1+ Spigot-based server. A MySQL database and (optionally) Redis are also needed if you wish to run the plugin across multiple servers on a proxy network.

1. Place the plugin jar file in the `/plugins/` directory of each Spigot server you want to install it on.
2. Start, then stop every server to let HuskTowns generate the config file.
3. Navigate to the HuskTowns config file on each server (`~/plugins/HuskTowns/config.yml`) 
4. Configure the plugin to your liking. If you are running HuskTowns across multiple servers, enable `cross_server` mode and fill in your MySQL credentials, remembering to change the database type to `MYSQL` as well.
5. You can also modify the level requirements in `~/levels.yml`, the default town rule settings in `~/rules.yml` and the town roles in `~/roles.yml`
6. Start every server again and HuskTowns should have completed  installation!

## Development
To build HuskTowns, simply run the following in the root of the repository:

```bash
./gradlew clean build
```

### License
HuskTowns is licensed under the Apache 2.0 license.

- [License](https://github.com/WiIIiam278/HuskTowns/blob/master/LICENSE)

Contributions to the project are welcome&mdash;feel free to open a pull request with new features, improvements and/or fixes!

### Support
Due to its complexity, official binaries and customer support for HuskTowns is provided through a paid model. This means that support is only available to users who have purchased a license to the plugin from Spigot, Polymart, Craftaro, or BuiltByBit and have provided proof of purchase. Please join our Discord server if you have done so and need help!

## Translations
Translations of the plugin locales are welcome to help make the plugin more accessible. Please submit a pull request with your translations as a `.yml` file. ([More info&hellip;](https://william278.net/docs/husktowns/translations))

- [Locales Directory](https://github.com/WiIIiam278/HuskTowns/tree/master/common/src/main/resources/locales)
- [English Locales](https://github.com/WiIIiam278/HuskTowns/tree/master/common/src/main/resources/locales/en-gb.yml)

## Links
- [Docs](https://william278.net/docs/husktowns) &mdash; Read the plugin documentation!
- [Spigot](https://www.spigotmc.org/resources/husktowns.92672/) &mdash; View the Spigot resource page (Also: [Polymart](https://polymart.org/resource/husktowns.1056), [Craftaro](https://craftaro.com/marketplace/product/husktowns.622), [BuiltByBit](https://builtbybit.com/resources/husktowns.34959/))
- [Issues](https://github.com/WiIIiam278/HuskTowns/issues) &mdash; File a bug report or feature request
- [Discord](https://discord.gg/tVYhJfyDWG) &mdash; Get help, ask questions (Proof of purchase required)
- [bStats](https://bstats.org/plugin/bukkit/HuskTowns/11265) &mdash; View plugin metrics

---
&copy; [William278](https://william278.net/), 2023. Licensed under the Apache-2.0 License.
