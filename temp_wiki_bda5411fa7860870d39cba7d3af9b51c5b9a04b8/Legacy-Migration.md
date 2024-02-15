This page details the process of migrating from HuskTowns v1.8.2 to HuskTowns v2.x.

HuskTowns' migrator will migrate all existing claims to HuskTowns v2.x&mdash;*except for Admin claims*. Due to the differences in how the claim system saves data, you will need to re-create admin claims after you've completed setup and migration using `/admintown claim`.

## Migration walkthrough
> ✅ Ensure all servers are offline and that users can't connect to your network before beginning migration

Please follow the steps below to upgrade from HuskTowns v1.8.2. If you're running an earlier version, please update to HuskTowns v1.8.2 first before migrating from v2.x.

### 1 Removing the old files
1. Navigate to `~/plugins/HuskTowns/` on your server
2. Move all the files out of this folder, placing them somewhere safe on your computer
3. Delete the HuskTowns v1.8.2 jar file from your `~/plugins/` folder and replace it with HuskTowns v2.x
4. Repeat steps 1-3 for each server running HuskTowns on your network (if you're using cross-server mode).
5. Start all your servers, then stop them shortly afterward.

### 2 Configuring the plugin
1. Navigate to `~/plugins/HuskTowns/` on your server and check that the plugin has generated new config files
2. Open the newly generated `config.yml` and your old v1 `config.yml` you copied over earlier side-by-side.
   1. If you previously used MySQL: set the database type to `MYSQL` and enter your connection credentials&mdash;but make sure that the table names are new names and don't match the existing table names used.
   2. If you previously used cross-server ("bungee") mode: ensure the cross-server mode setting is set to true in this file
   3. Fill in other settings to match your existing settings with their equivalents (e.g. the Admin town name, etc). 
   4. Make sure the disabled worlds lists in both config files match so that HuskTowns correctly generates the needed claim worlds.
3. *If you customised the town roles previously*: Open the newly generated `roles.yml` and your old v1 `config.yml` you copied over earlier side-by-side.
   1. Under "names" in the new file, enter the role name and associated weight as it is defined in the old config file
   2. Under "roles" in the new file, add the list of privileges for each weight. If you don't wish to assign any privileges at a role level, instead of the list, enter `[]` after the colon
4. *If you customised the town flag defaults previously:* Open the newly generated `rules.yml` and your old v1 `config.yml` you copied over earlier side-by-side.
   1. Fill in the default flag rules for each claim type, the wilderness, admin claims and unclaimable worlds as it is set up in your old config
   2. You can also edit `flags.yml` to customize the actions permitted for each flag
5. *If you are using cross-server mode*: Create a new file called `server.yml` and open your old v1 `config.yml` you copied earlier side-by-side
   1. At the top of the file, type `name: '<server>'`, replacing `<server>` with the ID *name of this server* as it is defined in your old config.
6. Update your [`messages-xx-xx.yml`](config-files) file to your liking. Note you can't use your old file as the interfaces and system messages have been completely rewritten.
7. Repeat steps 1-7 for each server running HuskTowns on your network
8. Start all your servers

### 3 Carrying out the migration
> ⚠️ If you have any HuskTowns v2 data in your table, it will be deleted when you start the migration process

1. In the console of one of your servers, type `husktowns migrate legacy`
2. Read the on-screen instructions carefully and ensure the parameters listed are correct. If you previously used MySQL, for instance, ensure the parameters match that of your MySQL database and that the database type is set to `MYSQL`
3. If you need to change a parameter, use `husktowns migrate legacy set <parameter_name> <value>` to do so
4. When you're happy, run `husktowns migrate legacy start` to begin the process. This can take several minutes to complete.
   1. You may receive a warning that it was unable to migrate the Administrators town over. This is expected, as the migrator doesn't support migrating admin claims.
   2. Be sure to re-create your admin claims after you've finished setup.
5. When the migration has finished, restart every server.

Your data should then have migrated. You can check that it has used the `town list` command in console or in-game.