# [![HuskTowns Banner](images/banner-graphic.png)](https://github.com/WiIIiam278/HuskTowns)
[![GitHub Actions](https://github.com/WiIIiam278/HuskTowns/actions/workflows/gradle.yml/badge.svg)](https://github.com/WiIIiam278/HuskTowns/actions/workflows/gradle.yml)
[![Discord](https://img.shields.io/discord/818135932103557162?color=7289da&logo=discord)](https://discord.gg/tVYhJfyDWG)

[Documentation, Guides & API](https://william278.net/docs/husktowns/Home) · [Resource Page](https://www.spigotmc.org/resources/husktowns.92672/) · [Bug Reports](https://github.com/WiIIiam278/HuskTowns/issues)

**HuskTowns** is a simple bungee-compatible Towny-style protection plugin for SpigotMC Minecraft servers. The plugin lets players form towns and claim land chunks on your server to protect them from grief. With a beautiful chat interface, easy to use commands - not to mention the ability for everything to work across multiple servers on a bungee network - your players will love using HuskTowns on your server.

## Features
* Let players create towns on your server.
* Towns can claim chunks to protect them from grief.
* Make plots within your town and assign them to players.
* Make farm areas within your town that all citizens can use.
* Towns can level up based on wealth - integrates with your economy!
* Set a town spawn and teleport to it.
* Customize town messages, change the name & transfer ownership.
* Coordinate with members of your town using town chat.
* Beautiful chat displays and clickable systems that are easy to use.
* Display claims on your server Dynmap, BlueMap or Squaremap.
* Works well with my other plugin, HuskHomes.
* All this works cross-server on a bungee network!
* All commands are intuitive and have permissions & TAB completion.
* Create administrator claims and apply bonuses to towns.
* Detailed configuration with a helpful plugin Wiki
* Customise town roles and fine-tune permissions.
* Has [a developer API](https://github.com/WiIIiam278/HuskTownsAPI)

| [<img src="https://img.youtube.com/vi/YnnprTNczeY/maxresdefault.jpg" height="300"/>](https://youtu.be/YnnprTNczeY) |
|--------------------------------------------------------------------------------------------------------------------|
| **Showcase:** [YouTube Video (8:30)](https://youtu.be/YnnprTNczeY)                                                 |

## Commands
* /town <create/disband/greeting/farewell...>
* /map, /claim, /unclaim
* /invite, /evict,
* /demote, /promote, /transfer
* /farm, /plot, /autoclaim
* /claimlist, /townlist, /admintown
* /adminclaim, /ignoreclaims, /townbonus
* /husktowns <help/about/...>

## Setup
### On a single server
1. Download HuskTowns.jar from the resource page
2. Place HuskTowns.jar in your server's plugin folder
3. (re)Start the server, then stop it again
4. Make configuration changes to the HuskTowns/config.yml file as neccessary
5. If you're using a permissions plugin, ensure permissions are set correctly
6. Start the server again and you are good to start using HuskTowns!

### On a proxy network
Requires a MySQL Database (v8.0+).

1. Download HuskTowns.jar from the resource page
2. Place the plugin in the plugin folders of **all** the servers you wish to run HuskTowns on
3. (re)Start all the servers you added the HuskTowns.jar to, then turn them off again
4. For each server, navigate to HuskTowns/config.yml and change the following settings
    1. Under `data_storage_options`, change the `storage_type` from `SQLite` to `mySQL`
    2. Fill in your mySQL credentials under `mysql_credentials`
    3. Under `bungee_options:`, set `enable_bungee_mode` to `true` and change the `server_id` to match the name of that server on the bungee network (e.g if you move to it using /server lobby, put "lobby" there)
    4. Modify other settings as appropriate
5. If you're using a permissions plugin, ensure permissions are set correctly
6. Start the servers you installed HuskTowns on and you should be good to go!

## Building
To build HuskTowns, simply run the following in the root of the repository:
```
./gradlew clean build
```

## License
HuskTowns is a premium resource. This source code is provided as reference only for those who have purchased the resource from an official source.

- [License](https://github.com/WiIIiam278/HuskTowns/blob/master/LICENSE)

## Contributing
A code bounty program is in place for HuskTowns, where developers making significant code contributions to HuskTowns may be entitled to a license at my discretion to use HuskTowns in commercial contexts without having to purchase the resource. Please read the information for contributors in the LICENSE file before submitting a pull request.

## Translation
Translations of the plugin locales are welcome to help make the plugin more accessible. Please submit a pull request with your translations as a `.yml` file.

- [Locales Directory](https://github.com/WiIIiam278/HuskTowns/tree/master/bukkit/src/main/resources/languages/)
- [English Locales](https://github.com/WiIIiam278/HuskTowns/blob/master/bukkit/src/main/resources/languages/en-gb.yml)

## bStats
This plugin uses bStats to provide me with [metrics about it's usage](https://bstats.org/plugin/bukkit/HuskTowns/11265).
You can turn metric collection off by navigating to `plugins/bStats/config.yml` and editing the config to disable plugin metrics.

## Links
- [Documentation, Guides & API](https://william278.net/docs/husktowns/Home)
- [Resource Page](https://www.spigotmc.org/resources/husktowns.92672/)
- [Bug Reports](https://github.com/WiIIiam278/HuskTowns/issues)
- [Discord Support](https://discord.gg/tVYhJfyDWG) (Proof of purchase required)

---
&copy; [William278](https://william278.net/), 2022. All rights reserved.
