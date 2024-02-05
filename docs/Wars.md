> **Warning:** This feature is currently marked as experimental, and you may experience bugs while using it. If you have feedback, please send it to me!

Town wars, added in HuskTowns v2.6, allow [[Towns]] which are marked as [mutual enemies](Relations) to go to war! This feature requires the [[Relations]] feature to be enabled.

## 1. Enabling Town Wars
To enable town wars, look for the `wars` subsection under the `relations` part of your config file. Ensure both `enabled` settings are set to `true`.

<details>
<summary>Town Wars (config.yml)</summary>

```yaml
relations:
    # Enable town relations (alliances and enemies). Docs: https://william278.net/docs/husktowns/town-relations/
    enabled: true
    wars:
      # Allow mutual enemy towns to agree to go to war. Requires town relations to be enabled. Wars consist of a battle between members, to take place at the spawn of the defending townDocs: https://william278.net/docs/husktowns/town-wars/
      enabled: true
```
</details>

## 2. Prerequisites for war
> **Warning:** A config setting exists for requiring a minimum percentage of town members to be online before declaring war, but this is not *yet* functional as of v2.6.

A War is between two towns, and a few requirements must be satisfied between both towns before going to war.

* Both towns must have at least one claim with a `/town spawn` point set within it.
* Both towns must be able to afford the minimum wager for a war (default value: `5000`)
* Both towns must not be on cooldown from a previous war (default value: `48 hours`)

### 2.1 Wagers
As part of declaring war, towns must agree to a wager to go to war with another town, the sum of which will be awarded to the victor. A minimum wager is required to declare war; this is configurable in `config.yml` (and defaults to `5000`). If a war ends in a stalemate (see below), the wager will be lost.

### 2.2 Cooldown
After finishing a war, a cooldown will be in place preventing that town from going to war again immediately. After the cooldown has elapsed, the town will be able to go to war again.

## 2.3 Declaring and going to war
If the requirements are met, the user with the `DECLARE_WAR` privilege (defaults to the Mayor role) can declare war with `/war declare <town> <wager>`. War declarations are sent cross-server (if this is in use), but the war battle itself will take place on the server the defending town's spawn is set.

The sender of the war declaration is considered the attacking town. The defending town will receive a message informing them that a war declaration request has been made against them. They must then accept the terms of war (including the proposed wager) with `/war accept`. A war declaration will expire after a configurable period of time (default: `10 minutes`), after which another one must be sent.

## 2.4 Rules of war
Wars are centered in a war zone around the defending town's `/town spawn` point. The war zone has a radius (default: `128 blocks`), and exiting this radius is not permitted (see below). This gives the defender a home ground advantage to prepare appropriate defensive measures. As soon as a war declaration has been accepted, war begins and users will be teleported.

A brief overview message will be displayed after a war.

### 2.5 Teleporting into battle
All online town members (including members on servers other than the one the war is set to take place on) will be teleported to the war area.

* For members of the *defending town*, all members will be teleported to the defined `/town spawn` point.
* For members of the *attacking town*, all online members will be teleported to a random safe ground position that is a radial distance `X` blocks away from the defending town's spawn (where `X` is the `war zone radius / 2`). 

### 2.6 Wartime Town Rules
During wartime, special Town Rules apply to town claims within a defending town; you can configure these to suit your server's flavour of chaos in the [`rules.yml`](config-files) file, such as enabling block destruction, explosions, etc. By default these rules just permit PvP.

<details>
<summary>Default Wartime Town Rules (rules.yml)</summary>

```yaml
# Default rules when a town is at war (only used during a town war)
wartime_rules:
  public_interact_access: true
  public_build_access: true
  monster_spawning: true
  pvp: true
  explosion_damage: true
  public_farm_access: true
  mob_griefing: true
  fire_damage: true
  public_container_access: true
```
</details>

### 2.7 PvP wars during war
Users participating in a war have PvP privileges within the defined war zone and are able to attack other players. Note that the standard town friendly fire setting in `config.yml` still applies during wartime.

Users not currently participating in a war (either because they are from another town or have died/fled/disconnected from battle) will adhere to the standard PvP rules based on their current location.

### 2.8 Death, fleeing & disconnecting
Participants of war are removed from the war if:

* They are killed or otherwise die
* They disconnect from the server (this includes changing server)
* They flee the war zone radius (this includes through the use of teleportation commands)

A special message will appear in chat on the server the war is taking place if a user dies or flees a war.

### 2.9 iewing the war status
The `/town war` command will display an overview in chat indicating the current status of a town war. This includes the wager on the war, the start time, the location, and a bar showing the current tides of battle.

![Screenshot of the HuskTowns Town War status screen](https://github.com/WiIIiam278/HuskTowns/assets/31187453/86a84fcd-6b19-45bc-bd6f-96fc66ed16bb)

Current war participants will also see a Boss Bar displaying the number of members of the opposing town still alive.

A small "currently at war with" notice will also be displayed on the Town Overview menu (`/town info`) for towns at war, which can be clicked to view the War Status menu.

### 2.10 Victory conditions
A war is won if there are no online remaining war participants on the opposing town.

A war can also be surrendered by either town with the `/war surrender` command; the opposing town will be declared the victor. This requires the `DECLARE_WAR` privilege.

### 2.11 Stalemate conditions
Wars will time out as a stalemate after 3 hours. 

Additionally, If a server restarts during a war, the war will also be cleared and marked as a stalemate after it boots back up.

## 3. Configuring Town Wars
Below is the full section of the [`config.yml`](config-files) file relevant to town wars, located under the `relations` section in the file.

<details>
<summary>Town Wars Config (config.yml)</summary>

```yaml
wars:
    # Allow mutual enemy towns to agree to go to war. Requires town relations to be enabled. Wars consist of a battle between members, to take place at the spawn of the defending townDocs: https://william278.net/docs/husktowns/town-wars/
    enabled: false
    # The number of hours before a town can be involved with another war after finishing one
    cooldown: 48
    # How long before pending declarations of war expire
    declaration_expiry: 10
    # The minimum wager for a war. This is the amount of money each town must pay to participate in a war. The winner of the war will receive both wagers.
    minimum_wager: 5000.0
    # The color of the boss bar displayed during a war
    boss_bar_color: RED
    # The minimum number of members online in a town for it to be able to participate in a war (%).
    required_online_membership: 50.0
    # The radius around the defending town's spawn, in blocks, where battle can take place. (Min: 16)
    war_zone_radius: 128
```
</details>