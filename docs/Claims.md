[[Towns]] can claim chunks of your server worlds to protect them from griefing and to build in. This page will explain how to make and manage claims.

## 1. Making claims
Claims are chunk-based. To claim a chunk of land for your town, type `/town claim`.

There are a few different types of claims you can make. By default, only members of your town with the `trusted_access` privilege (i.e. Trustees and the Mayor in the default role hierarchy) can build in town claims, but you can create "farm" claims to designate public areas of your town any member can build in and "plot" claims to allocate parts of your town to members if you wish as well.

Outside of claims (the "Wilderness"), anyone is able to build by default. You can customise the "town rules" (see below for more information) of the Wilderness by editing the `rules.yml` file.

### 1.1 The claim map
You can use the `/town map` command to view a map of nearby claims on a grid view in chat. This will also highlight the chunk you're standing on with colored particles around the edges.

Clicking a dark gray ("Wilderness") square will claim that chunk for your town and reopen the map, to allow for efficient claiming.

### 1.2 Town deeds / claims list
To view a list of claims made by any given town on the server you're on, use the `/town deeds [name]` command.

### 1.3 Auto-claiming
You can toggle auto-claiming, which automatically claims chunks for your town as you walk into them, using the `/town autoclaim` command.

### 1.4 Inspecting claims
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

## 2. Types of claims
When a claim has been created, it starts out as a regular claim, but you can change it to a farm or plot claim if you wish, which have special properties and allow for more fine control for creating districts within your settlement.

### 2.1 Town plots
To make or manage a town plot claim, you must be standing in it.

To make a town plot from a regular claim, use `/town plot` and members will then be able to use `/plot claim` to claim the plot while it is vacant. Alternatively, you can assign someone to the plot with `/town plot add <player>`. Note the `<player>` does not actually have to be a town member. Players added to a plot have full access to build within it.

You can designate someone as a "manager" of a town plot, which will let them add others to the plot as well using the previously mentioned command. You can do this with `/town plot add <player> manager`.

You can remove someone from a plot with `/town plot remove <player>` and view a simple list of plot members with `/town plot list`.

### 2.2 Town farms
To make a town claim into a town farm claim, stand in it and type `/town farm`. Mob spawners and crops in town farms spawn/grow at boosted rates based on your town's level. Any member of your town can break and place farm blocks, crops, interact with mobs as well as access containers in town farms. They can't, however, build or break most structures.

### 3. Town rules
You can set "flags" that change the properties of how users interact within claims, for example, such as by enabling/disabling PvP, explosions, fire spread and monster spawning or changing build, container and interact access for non-town members.

To edit the town flag rules, use the `/town rules` clickable menu to edit the flag properties for the three different types of claims (claims, farms and plots).

Note that if you are using [[Wars]], special rules apply to participants in towns at war.

## 3.1 Unclaimable worlds
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

### 4. Admin claims
Administrators can make claims to protect pieces of land from harm. As an administrator, use `/admintown claim` to make a claim. You can use `/admintown unclaim` to delete an existing claim if one is in the way, or remove an admin claim if you wish to get rid of it.

Admin claims have their own special set of town rules, which administrators can configure in the `rules.yml` file.

