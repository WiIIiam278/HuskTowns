Once you have [installed HuskTowns](setup) on your server, in order to get started using the plugin, you'll need to either make a new town, or accept an invitation from another player to join one. Players can only be in one town at a time and each town has a single mayor.

## 1 Creating a town
To make a town, type `/town create <name>`. This will make a new town at level 1 with the given name. 

If your server has the `towns.require_first_level_collateral` setting enabled, then creating a town will cost the price of "level 1" as defined in [`levels.yml`](config-files). The name mustn't contain any whitespace characters.

### 1.1 Customising
You can change the color, bio, greeting and farewell messages of your town. Each changes the way your town appears to other players.
* **Town Color**—the color of your town on claim maps. Change with `/town color <#rgb>`
* **Town Bio**—the bio of your town that appears on your `/town info <name>` page, and on hover in the `/town list`. Change with `/town bio <message>`
* **Town Greeting Message**—the message that will be shown to players as they walk into your claims. Change with `/town greeting <message>`
* **Town Farewell Message**—the message that will be shown to players as they walk out of your claims. Change with `/town farewell <message>`

You can also rename the town using `/town rename <name>` if you've changed your mind.

### 1.2 Town about page
All towns have a town about page, which shows a number of important pieces of information about each town, such as its level, population count, claim count, mayor, bank balance, spawn position, etc.

If you are the member of a town, running the `/town` command on its' own will show your town about page. If you want to view another town's about page, use `/town about <name>`.

### 1.3 Town list
You can view a list of towns using the `/town list` command. You can use the pagination and filter buttons to change the list sorting if a lot of towns have been made. Clicking the name of towns on this list will show their town about page.

### 1.4 Town chat
You can send messages privately to fellow town members using `/town chat <message>`.

## 2 Adding members
You can invite members to your town using `/town invite <player>`. Note you can only invite players who aren't currently the member of a town.

### 2.1 Managing access
You can promote and demote members up and down the town role hierarchy, as it has been set up on the server. To promote a member, use `/town promote <member>`. Likewise, to demote a user, use `/town demote <member>`. Note you can't promise members to or who are an equal or higher role in the town history than yourself.

### 2.2 Privileges
Each role in the history has associated privileges, including inherited privileges from roles lower down in the hierarchy. For example, by default, only the mayor can rename the town and only trustees can invite other members.

<details>
<summary>Default role hierarchy</summary>

| Role Weight | Role Name | Privileges                                                                                                                                                               |
|:-----------:|:---------:|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|      3      |   Mayor   | `set_bio`, `evict`, `promote`, `demote`, `withdraw`, `level_up`, `set_rules`, `rename`, `set_color`                                                                      |
|      2      |  Trustee  | `set_farm`, `set_plot`, `manage_plot_members`, `trusted_access`, `unclaim`, `claim`, `set_greeting`, `set_farewell`, `invite`, `set_spawn`, `spawn_privacy`, `view_logs` |
|      1      | Resident  | `deposit`, `chat`, `spawn`                                                                                                                                               |

</details>

See [[Town Roles]] for more information on role customisation.

### 2.3 Town census / member list
To view the list of members for any given town, grouped by role, use the `/town census [name]` command.

### 2.4 Evicting members
If you have the privilege to do so, you can evict a member with `/town evict <member>`. Note you can't evict members with an equal or higher role than yourself.

### 2.5 Leaving a town
If you want to leave your town, you can do so with `/town leave`. The mayor can't leave their own town; they must first transfer ownership or delete it instead.

### 2.6 Transferring and deleting
The mayor of the town can transfer ownership of it to another member of the town using `/town transfer <member>`. They can also choose to delete the town with `/town delete` if they wish, which will also remove all the towns' claims.

## 3 Levelling up your town
Towns have a bank balance ("coffers") and level, which starts at 1. By levelling up your town, you can increase the maximum number of claims and members your town can have, as well as gain boosted crop growth rates and mob spawner spawn rates within your town's farm chunks.

To level up your town, you must deposit money into your town coffers using `/town deposit <amount>`. When your town meets the level-up threshold, you can then use `/town levelup` to spend money out of your town coffers to raise your town level by 1. Members with privileges can also do `/town withdraw <amount>` to withdraw from the town coffers at any time.

### 3.1 Level requirements
If you're the administrator, you can edit these by modifying the `levels.yml` file.

<details>
<summary>Default level requirements</summary>

| Level | Upgrade Cost  | Claims | Members |
|:-----:|---------------|:------:|:-------:|
|   1   | 2,000         |   6    |    5    |
|   2   | 4,000         |   12   |   10    |
|   3   | 8,000         |   18   |   15    |
|   4   | 16,000        |   24   |   20    |
|   5   | 32,000        |   30   |   25    |
|   6   | 64,000        |   36   |   30    |
|   7   | 128,000       |   42   |   35    |
|   8   | 256,000       |   48   |   40    |
|   9   | 512,000       |   54   |   45    |
|  10   | 1,024,000     |   60   |   50    |
|  11   | 2,048,000     |   66   |   55    |
|  12   | 4,096,000     |   72   |   60    |
|  13   | 8,192,000     |   78   |   65    |
|  14   | 16,384,000    |   84   |   70    |
|  15   | 32,768,000    |   90   |   75    |
|  16   | 65,536,000    |   96   |   80    |
|  17   | 131,072,000   |  102   |   85    |
|  18   | 262,144,000   |  108   |   90    |
|  19   | 524,288,000   |  114   |   95    |
|  20   | 1,048,576,000 |  120   |   100   |

</details>

## 4 Making claims
Claims are chunk-based. To claim a chunk of land for your town, type `/town claim`. 

There are a few different types of claims you can make. By default, only members of your town with the `trusted_access` privilege (i.e. Trustees and the Mayor in the default role hierarchy) can build in town claims, but you can create "farm" claims to designate public areas of your town any member can build in and "plot" claims to allocate parts of your town to members if you wish as well.

Outside of claims (the "Wilderness"), anyone is able to build by default. You can customise the "town rules" (see below for more information) of the Wilderness by editing the `rules.yml` file.

### 4.1 The claim map
You can use the `/town map` command to view a map of nearby claims on a grid view in chat. This will also highlight the chunk you're standing on with colored particles around the edges. 

Clicking a dark gray ("Wilderness") square will claim that chunk for your town and reopen the map, to allow for efficient claiming.

### 4.2 Auto-claiming
You can toggle auto-claiming, which automatically claims chunks for your town as you walk into them, using the `/town autoclaim` command.

### 4.3 Inspecting claims
You can right-click a chunk with the claim inspection tool to see if it has been claimed. This will highlight the chunk you're standing on with colored particles around the edges if it has been claimed and tell you which town it belongs to. 

The claim inspection tool is a `stick` by default and can be changed in the config.yml file if you wish. 

<details>
<summary>Inspection tool (config.yml)</summary>

```yaml
general:
  # Change this to a different Minecraft item ID if you wish.
  inspector_tool: minecraft:stick
```

</details>

### 4.4 Town plots
To make or manage a town plot claim, you must be standing in it. 

To make a town plot from a regular claim, use `/town plot`. You can then add someone to the plot with `/town plot add <player>`. Note the `<player>` does not actually have to be a town member. Players added to a plot have full access to build within it.

You can designate someone as a "manager" of a town plot, which will let them add others to the plot as well using the previously mentioned command. You can do this with `/town plot add <player> manager`.

You can remove someone from a plot with `/town plot remove <player>` and view a simple list of plot members with `/town plot list`.

### 4.5 Town farms
To make a town claim into a town farm claim, stand in it and type `/town farm`. Mob spawners and crops in town farms spawn/grow at boosted rates based on your town's level. Any member of your town can break and place farm blocks, crops, interact with mobs as well as access containers in town farms. They can't, however, build or break most structures.

### 4.6 Town deeds / claims list
To view a list of claims made by any given town on the server you're on, use the `/town deeds [name]` command.

### 4.7 Town rules
You can set "flags" that change the properties of how users interact within claims, for example, such as by enabling/disabling PvP, explosions, fire spread and monster spawning or changing build, container and interact access for non-town members. 

To edit the town flag rules, use the `/town rules` clickable menu to edit the flag properties for the three different types of claims (claims, farms and plots).

### 4.8 Admin claims
Administrators can make claims to protect pieces of land from harm. As an administrator, use `/admintown claim` to make a claim. You can use `/admintown unclaim` to delete an existing claim if one is in the way, or remove an admin claim if you wish to get rid of it.

Admin claims have their own special set of town rules, which administrators can configure in the `rules.yml` file.

## 4.9 Unclaimable worlds
If you are an administrator, you can define "unclaimable worlds" in the server config.yml file, which will prevent users from claiming land in those worlds. By default, the `world_nether` and `world_the_end` worlds are unclaimable worlds, meaning players can't make town claims in those dimensions.

Unclaimable worlds have their own special set of town rules, which administrators can configure in the `rules.yml` file.

<details>
<summary>Unclaimable worlds (config.yml)</summary>

```yaml
general:
  # Add worlds to this list to mark them as unclaimable
  unclaimable_worlds:
  - world_nether
  - world_the_end
```

</details>

## 5 The town spawn
Your town has a spawn point that you can teleport to, which must be located in one of your town claims. To set your town spawn, use `/town setspawn`. Members of the town can then return to it with `/town spawn`. 

### 5.1 Spawn privacy
If you wish to allow members from outside your town to teleport to your town spawn, use `/town privacy public` to make the spawn public. Anyone can then use `/town spawn <name>` to pay a visit.