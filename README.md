[![Header](https://i.imgur.com/JckKnZZ.png "Header")](https://github.com/WiIIiam278/HuskTownsDocs/)
# HuskTowns
[![Discord](https://img.shields.io/discord/818135932103557162?color=7289da&logo=discord)](https://discord.gg/tVYhJfyDWG)

**HuskTowns** is a simple bungee-compatible Towny-style protection plugin for SpigotMC Minecraft servers. The plugin lets players form towns and claim land chunks on your server to protect them from grief. With a beautiful chat interface, easy to use commands - not to mention the ability for everything to work across multiple servers on a bungee network - your players will love using HuskTowns on your server.

## Disclaimer
This source code is provided as reference to licensed individuals that have purchased the HuskTowns plugin from any of the official sources it is provided; [Spigot](https://www.spigotmc.org/resources/husktowns.92672/), [Polymart](https://polymart.org/resource/husktowns.1056) or [Songoda](https://songoda.com/marketplace/product/husktowns-a-simple-bungee-compatible-towny-style-protection-plugin.622). The availability of this code does not grant you the rights to re-distribute or share this source code outside this intended purpose.

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
* Display claims on your server Dynmap, BlueMap or Pl3xMap.
* Works well with my other plugin, HuskHomes.
* All this works cross-server on a bungee network!
* All commands are intuitive and have permissions & TAB completion.
* Create administrator claims and apply bonuses to towns.
* Detailed configuration with a helpful plugin Wiki.
* Has [a developer API](https://github.com/WiIIiam278/HuskTownsAPI)!

## Showcase
|[<img src="https://img.youtube.com/vi/YnnprTNczeY/maxresdefault.jpg" height="300" />](https://youtu.be/YnnprTNczeY)|
:-:
|[YouTube Video (8:30)](https://youtu.be/YnnprTNczeY)|

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
### For a single server
1. Download HuskTowns.jar from the resource page
2. Place HuskTowns.jar in your server's plugin folder
3. (re)Start the server, then stop it again
4. Make configuration changes to the HuskTowns/config.yml file as neccessary
5. If you're using a permissions plugin, ensure permissions are set correctly
6. Start the server again and you are good to start using HuskTowns!

### On a bungee network
> :warning: You will need a mySQL Database setup to enable Bungee features
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

## bStats
This plugin uses bStats to provide me with [metrics about it's usage](https://bstats.org/plugin/bukkit/HuskTowns/11265).
You can turn metric collection off by navigating to `plugins/bStats/config.yml` and editing the config to disable plugin metrics.

## Support
* Report bugs: [Click here](https://github.com/WiIIiam278/HuskTowns/issues)
* Check the HuskTowns Wiki: [Click here](https://github.com/WiIIiam278/HuskTowns/wiki)
* If you still need support, join the [HuskHelp Discord](https://discord.gg/tVYhJfyDWG)!
    * Proof of purchase is required for support.