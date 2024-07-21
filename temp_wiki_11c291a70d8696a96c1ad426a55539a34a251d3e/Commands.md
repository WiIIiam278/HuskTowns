HuskTowns has a wide range of commands, each having corresponding permission nodes. Each command has a help menu that can be accessed to view these lists in-game.

## /town command
The `/town` command (base permission: `husktowns.command.town`) is the entry point for all operations. In addition to these permissions, the player may need to have a prerequisite *privilege* to use the command in the town they are in, based on their town role.

| Command            | Description                                 | Permission                          |
|--------------------|---------------------------------------------|-------------------------------------|
| `/town help`       | View the list of town commands              | `husktowns.command.town.help`       |
| `/town create`     | Create a town                               | `husktowns.command.town.create`     |
| `/town about`      | View information about a town               | `husktowns.command.town.about`      |
| `/town list`       | View a list of towns                        | `husktowns.command.town.list`       |
| `/town invite`     | Invite someone to your town                 | `husktowns.command.town.invite`     |
| `/town claim`      | Claim a chunk                               | `husktowns.command.town.claim`      |
| `/town unclaim`    | Remove a claim                              | `husktowns.command.town.unclaim`    |
| `/town autoclaim`  | Toggle auto-claiming chunks as you walk     | `husktowns.command.town.autoclaim`  |
| `/town map`        | View a map of nearby town claims            | `husktowns.command.town.map`        |
| `/town promote`    | Promote a member up the role hierarchy      | `husktowns.command.town.promote`    |
| `/town demote`     | Demote a member down the role hierarchy     | `husktowns.command.town.demote`     |
| `/town evict`      | Evict a member from the town                | `husktowns.command.town.evict`      |
| `/town leave`      | Leave the town                              | `husktowns.command.town.leave`      |
| `/town farm`       | Make a claim into a town farm               | `husktowns.command.town.farm`       |
| `/town plot`       | Make a claim into a plot and manage it      | `husktowns.command.town.plot`       |
| `/town rules`      | Edit the town rules                         | `husktowns.command.town.rules`      |
| `/town deposit`    | Deposit into the town coffers               | `husktowns.command.town.deposit`    |
| `/town withdraw`   | Withdraw from the town coffers              | `husktowns.command.town.withdraw`   |
| `/town level`      | Pay to level-up the town                    | `husktowns.command.town.level`      |
| `/town bio`        | Edit the town bio                           | `husktowns.command.town.bio`        |
| `/town greeting`   | Edit the town greeting message              | `husktowns.command.town.greeting`   |
| `/town farewell`   | Edit the town farewell message              | `husktowns.command.town.farewell`   |
| `/town color`      | Edit the town color                         | `husktowns.command.town.color`      |
| `/town rename`     | Rename the town                             | `husktowns.command.town.rename`     |
| `/town spawn`      | Teleport to a town spawn                    | `husktowns.command.town.spawn`      |
| `/town setspawn`   | Set the town spawn                          | `husktowns.command.town.setspawn`   |
| `/town clearspawn` | Clear the town spawn                        | `husktowns.command.town.clearspawn` |
| `/town privacy`    | Edit the privacy of the town spawn          | `husktowns.command.town.privacy`    |
| `/town chat`       | Send a message to the town chat             | `husktowns.command.town.chat`       |
| `/town player`     | View which town a player is a member of     | `husktowns.command.town.player`     |
| `/town deeds`      | View a list of town claims on this server   | `husktowns.command.town.deeds`      |
| `/town census`     | View a list of town members and their roles | `husktowns.command.town.census`     |
| `/town relations`  | Manage [[Relations]] if enabled        | `husktowns.command.town.relations`  |
| `/town war`        | View and declare [[Wars]] if enabled   | `husktowns.command.town.war`        |
| `/town log`        | View the town audit log                     | `husktowns.command.town.log`        |
| `/town transfer`   | Transfer ownership of the town to someone   | `husktowns.command.town.transfer`   |
| `/town disband`    | Delete the town                             | `husktowns.command.town.disband`    |

The `husktowns.command.town.*` permission can be used to grant all town (`/town`) commands.

## /admintown command
The `/admintown` command (base permission: `husktowns.command.admintown`) is for carrying out admin operations on towns or the world.

| Command                   | Description                                   | Permission                                 |
|---------------------------|-----------------------------------------------|--------------------------------------------|
| `/admintown help`         | View the list of administrator commands       | `husktowns.command.admintown.help`         |
| `/admintown claim`        | Create an admin claim                         | `husktowns.command.admintown.help`         |
| `/admintown unclaim`      | Delete a claim                                | `husktowns.command.admintown.help`         |
| `/admintown ignoreclaims` | Toggle ignoring/respecting claim access       | `husktowns.command.admintown.ignoreclaims` |
| `/admintown chatspy`      | Toggle spying on town chat messages           | `husktowns.command.admintown.chatspy`      |
| `/admintown delete`       | Delete a town                                 | `husktowns.command.admintown.delete`       |
| `/admintown takeover`     | Join and assume ownership of a town           | `husktowns.command.admintown.takeover`     |
| `/admintown balance`      | Set or change the balance of a town           | `husktowns.command.admintown.balance`      |
| `/admintown setlevel`     | Set the level of a town                       | `husktowns.command.admintown.setlevel`     |
| `/admintown prune`        | [Prune inactive towns](Inactive-Town-Pruning) | `husktowns.command.admintown.prune`        |
| `/admintown advancements` | [Check town advancements](Advancements)  | `husktowns.command.admintown.advancements` |
| `/admintown bonus`        | Apply or manage town bonuses                  | `husktowns.command.admintown.bonus`        |


The `husktowns.command.admintown.*` permission can be used to grant all administrator (`/admintown`) commands.

## /husktowns command
The `/husktowns` command (base permission: `husktowns.command.husktowns`) is for plugin system maintenance and information.

| Command              | Description                               | Permission                            |
|----------------------|-------------------------------------------|---------------------------------------|
| `/husktowns help`    | View the list of system commands          | `husktowns.command.husktowns.help`    |
| `/husktowns about`   | View the plugin about menu                | `husktowns.command.husktowns.about`   |
| `/husktowns update`  | Check for plugin updates                  | `husktowns.command.husktowns.update`  |
| `/husktowns reload`  | Reload the plugin locales                 | `husktowns.command.husktowns.reload`  |
| `/husktowns migrate` | Carry out a [migration](legacy-migration) | `husktowns.command.husktowns.migrate` |

The `husktowns.command.husktowns.*` permission can be used to grant all system (`/husktowns`) commands.

## Additional permissions
The following special permissions can also be used
* `husktowns.admin_claim_access` - Access to be able to build in any admin claim
* `husktowns.spawn_privacy_bypass` - Bypass spawn privacy limitations, letting you teleport to any town spawn