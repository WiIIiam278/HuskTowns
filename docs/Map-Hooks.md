HuskTowns has optional support for integrating with popular server web map plugins. The following map plugins are supported:
* 🗺️ [Dynmap](https://github.com/webbukkit/dynmap) 
* 💙 [BlueMap](https://www.spigotmc.org/resources/bluemap.83557/)
* -  [squaremap](https://modrinth.com/plugin/squaremap)

If enabled, HuskTowns will add markers to your map showing the location of public homes and warps all over your server! You can then click on them to bring up information about the home/warp and a description. This can be great to allow players to plan their next journey and help identify locations on your server world.

## Setup
1. Make sure you have one of the supported map plugins installed on your server and that it is running the latest version.
2. Turn off your server and navigate to `plugins/HuskTowns/config.yml`
3. Scroll down to the `web_map_hook` section under `general`
4. Ensure `enabled` is set to true and configure the marker set name to your liking
5. Save the config, restart the server and your web map should be populated with town claims