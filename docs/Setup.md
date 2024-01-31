This will walk you through installing HuskTowns on either your Spigot server, or proxied network of Spigot servers.

## Requirements
> **Note:** If the plugin fails to load, please check that you are not running an [incompatible version combination](Unsupported-Versions)

* A Spigot-based Minecraft server (1.17.1 or higher, Java 17+)
* (For proxy network support) A proxy server (Velocity, BungeeCord) and MySQL (v8.0+) database
* (For optional redis support) A Redis database (v5.0+)

## Single-server Setup Instructions
These instructions are for simply installing HuskTowns on one Spigot/Paper server.

### 1. Install the jar
- Place the plugin jar file in the `/plugins/` directory of your Spigot server.
### 2. Restart the server and configure
- Start, then stop your server to let HuskTowns generate the config file.
- You can now edit the config files to your liking. 
- For the `roles.yml` file especially, make sure to set that file up now as adding new roles later isn't possible
### 3. Turn on your server
- Start your server again and enjoy HuskTowns!

## Multi-server Setup Instructions
These instructions are for installing HuskTowns on multiple Spigot servers and having them network together. A MySQL database (v8.0+) is required.

### 1. Install the jar
- Place the plugin jar file in the `/plugins/` directory of each Spigot server.
- You don't need to install HuskTowns as a proxy plugin.
### 2. Restart the server and configure
- Start, then stop every server to let HuskTowns generate the config file.
- Advanced users: If you'd prefer, you can just create one config.yml file and create symbolic links in each `/plugins/HuskTowns/` folder to it to make updating it easier.
### 3. Configure servers to use cross-server mode
- Navigate to the HuskTowns general config file on each server (`~/plugins/HuskTowns/config.yml`)
- Under `database`, set `type` to `MYSQL`
- Under `mysql`/`credentials`, enter the credentials of your MySQL database server.
- Scroll down and look for the `cross_server` section. Set `enabled` to `true`.
- You can additionally configure a Redis server to use for network messaging, if you prefer (set the `messenger_type` to `REDIS` if you do this).
- Update your `levels.yml`, `rules.yml` and `roles.yml` files to your liking. For the roles.yml file especially, make sure to set that file up now as adding new roles later isn't possible
- Save the configs file. Make sure you've updated the files on every server.
### 4. Restart servers and set server.yml values
- Restart each server again. A `server.yml` file should generate inside (`~/plugins/HuskTowns/`)
- Set the `name` of the server in this file to the ID of this server as defined in the config of your proxy (e.g. if this is the "hub" server you access with `/server hub`, put "hub" here)
### 5. Restart your servers one last time
- Provided your MySQL database credentials were correct, your network should now be setup to use HuskTowns!
- You can delete the `HuskTownsData.db` SQLite flat file that was generated, if you would like.

## Next steps
* [[Getting Started]]
* [[Config Files]]
* [Commands & Permissions](Commands)