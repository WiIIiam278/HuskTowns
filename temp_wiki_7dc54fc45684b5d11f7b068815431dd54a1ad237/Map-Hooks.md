HuskTowns has optional support for integrating with popular server web map plugins. The following map plugins are supported:
* 🗺️ [Dynmap](https://github.com/webbukkit/dynmap) 
* 💙 [BlueMap](https://www.spigotmc.org/resources/bluemap.83557/)
* 🔍 [Pl3xMap](https://modrinth.com/plugin/pl3xmap)

If enabled, HuskTowns will add a layer to your map highlighting chunks that are claimed by the different towns on your server! This is great for visualising where your communities are being built across your server, and to help players plan their next journey.

## Setup
1. Make sure you have one of the supported map plugins installed on your server and that it is running the latest version.
2. Turn off your server and navigate to `plugins/HuskTowns/config.yml`
3. Scroll down to the `web_map_hook` section under `general`
4. Ensure `enabled` is set to true and configure the marker set name to your liking
5. Save the config, restart the server and your web map should be populated with town claims
