This page contains the configuration structure for HuskTowns.

## Configuration structure
📁 `plugins/HuskTowns/`
  - 📄 `config.yml`: General plugin configuration
  - 📄 `flags.yml`: Flag definition configuration
  - 📄 `levels.yml`: Town level requirements and limits
  - 📄 [`roles.yml`](Roles): Town role hierarchy (see [[Roles]])
  - 📄 `rules.yml`: Default town/wilderness claim/flag rules
  - 📄 `server.yml`: (Cross-server setups only) Server ID configuration
  - 📄 [`advancements.json`](Advancements): Town advancements JSON file (see [[Advancements]])
  - 📄 [`messages-xx-xx.yml`](translations): Plugin locales, formatted in MineDown (see [[Translations]])

## Example files
<details>
<summary>config.yml</summary>

```yaml
# ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
# ┃       HuskTowns Config       ┃
# ┃    Developed by William278   ┃
# ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
# ┣╸ Information: https://william278.net/project/husktowns
# ┣╸ Config Help: https://william278.net/docs/husktowns/config-files/
# ┗╸ Documentation: https://william278.net/docs/husktowns

# Locale of the default language file to use. Docs: https://william278.net/docs/husktowns/translations
language: en-gb
# Whether to automatically check for plugin updates on startup
check_for_updates: true
# Aliases to use for the /town command.
aliases:
  - t
# Database settings
database:
  # Type of database to use (SQLITE, MYSQL, MARIADB)
  type: SQLITE
  # Specify credentials here for your MYSQL or MARIADB database
  credentials:
    host: localhost
    port: 3306
    database: HuskTowns
    username: root
    password: pa55w0rd
    parameters: ?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8
  # MYSQL / MARIADB database Hikari connection pool properties. Don't modify this unless you know what you're doing!
  connection_pool:
    size: 10
    idle: 10
    lifetime: 1800000
    keepalive: 0
    timeout: 5000
  # Names of tables to use on your database. Don't modify this unless you know what you're doing!
  table_names:
    meta_data: husktowns_metadata
    claim_data: husktowns_claim_worlds
    user_data: husktowns_users
    town_data: husktowns_town_data
# Cross-server settings
cross_server:
  # Whether to enable cross-server mode
  enabled: false
  # The cluster ID, for if you're networking multiple separate groups of HuskTowns-enabled servers.
  # Do not change unless you know what you're doing
  cluster_id: main
  # Type of network message broker to ues for data synchronization (PLUGIN_MESSAGE or REDIS)
  broker_type: PLUGIN_MESSAGE
  # Settings for if you're using REDIS as your message broker
  redis:
    host: localhost
    port: 6379
    # Password for your Redis server. Leave blank if you're not using a password.
    password: ''
    use_ssl: false
    # Settings for if you're using Redis Sentinels.
    # If you're not sure what this is, please ignore this section.
    sentinel:
      master_name: ''
      # List of host:port pairs
      nodes: []
      password: ''
# Cross-server settings
general:
  # How many items should be displayed per-page in chat menu lists
  list_items_per_page: 6
  # Which item to use for the inspector tool; the item that displays claim information when right-clicked.
  inspector_tool: minecraft:stick
  # How far away the inspector tool can be used from a claim. (in blocks)
  max_inspection_distance: 80
  # The slot to display claim entry/teleportation notifications in. (ACTION_BAR, CHAT, TITLE, SUBTITLE, NONE)
  notification_slot: ACTION_BAR
  # The width and height of the claim map displayed in chat when running the /town map command.
  claim_map_width: 9
  claim_map_height: 9
  # Whether town spawns should be automatically created when a town's first claim is made.
  first_claim_auto_setspawn: false
  # Whether to allow players to attack other players in their town.
  allow_friendly_fire: false
  # A list of world names where claims cannot be created.
  unclaimable_worlds:
    - world_nether
    - world_the_end
  # A list of town names that cannot be used.
  prohibited_town_names:
    - Administrators
    - Moderators
    - Mods
    - Staff
    - Server
  # Adds special advancements for town progression. Docs: https://william278.net/docs/husktowns/advancements/
  do_advancements: false
  # Enable economy features. Requires Vault and a compatible economy plugin.If disabled, or if Vault is not installed, the built-in town points currency will be used instead.
  economy_hook: true
  # Hook with LuckPerms to provide town permission contexts. Docs: https://william278.net/docs/husktowns/hooks
  luckperms_contexts_hook: true
  # Hook with PlaceholderAPI to provide placeholders. Docs: https://william278.net/docs/husktowns/hooks
  placeholderapi_hook: true
  # Use HuskHomes for improved teleportation. Docs: https://william278.net/docs/husktowns/hooks
  huskhomes_hook: true
  # Show town information on your Player Analytics web panel. Docs: https://william278.net/docs/husktowns/hooks
  plan_hook: true
  # Show town information on your server Dynmap, BlueMap or Pl3xMap. Docs: https://william278.net/docs/husktowns/hooks
  web_map_hook:
    # Enable hooking into web map plugins
    enabled: true
    # The name of the marker set to use for claims on your web map
    marker_set_name: Claims
# Town settings
towns:
  # Whether town names should be restricted by a regex. Set this to false to allow full UTF-8 names.
  restrict_town_names: true
  # Regex which town names must match. Names have a hard min/max length of 3-16 characters
  town_name_regex: '[a-zA-Z0-9-_]*'
  # Whether town bios/greetings/farewells should be restricted. Set this to false to allow full UTF-8.
  restrict_town_bios: true
  # Regex which town bios/greeting/farewells must match. A hard max length of 256 characters is enforced
  town_meta_regex: \A\p{ASCII}*\z
  # Require the level 1 cost as collateral when creating a town (this cost is otherwise ignored)
  require_first_level_collateral: false
  # The minimum distance apart towns must be, in chunks
  minimum_chunk_separation: 0
  # Require towns to have all their claims adjacent to each other
  require_claim_adjacency: false
  # Whether to spawn particle effects when crop growth or mob spawning is boosted by a town's level
  spawn_boost_particles: true
  # Which particle effect to use for crop growth and mob spawning boosts
  boost_particle: spell_witch
  # Relations settings
  relations:
    # Enable town relations (alliances and enemies). Docs: https://william278.net/docs/husktowns/relations/
    enabled: true
    # Town War settings
    wars:
      # Allow mutual enemy towns to agree to go to war. Requires town relations to be enabled. Wars consist of a battle between members, to take place at the spawn of the defending townDocs: https://william278.net/docs/husktowns/wars/
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
  # Admin Town settings for changing how admin claims look
  admin_town:
    name: Admin
    color: '#ff0000'
  # Settings for town pruning
  prune_inactive_towns:
    # Delete towns on startup who have had no members online within a certain number of days. Docs: https://william278.net/docs/husktowns/inactive-town-pruning/
    prune_on_startup: false
    # The number of days a town can be inactive before it will be deleted
    prune_after_days: 90
```
</details>

<details>
<summary>flags.yml</summary>

```yaml
# ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
# ┃    HuskTowns Flags Config    ┃
# ┃    Developed by William278   ┃
# ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
# ┣╸ This file is for configuring flags. Flag IDs map to a list of permitted operations.
# ┗╸ Config Help: https://william278.net/docs/husktowns/config-files

# A map of flag IDs to operations that flag permits.Display names of flags correspond to a "town_rule_name_" locale in your messages file.
flags:
  public_container_access:
    - CONTAINER_OPEN
  fire_damage:
    - FIRE_SPREAD
    - FIRE_BURN
  public_farm_access:
    - BLOCK_INTERACT
    - FARM_BLOCK_PLACE
    - FARM_BLOCK_BREAK
    - FARM_BLOCK_INTERACT
    - PLAYER_DAMAGE_ENTITY
  public_build_access:
    - CONTAINER_OPEN
    - BLOCK_PLACE
    - FARM_BLOCK_PLACE
    - PLAYER_DAMAGE_MONSTER
    - ENDER_PEARL_TELEPORT
    - BLOCK_INTERACT
    - USE_SPAWN_EGG
    - BREAK_HANGING_ENTITY
    - PLACE_HANGING_ENTITY
    - FARM_BLOCK_INTERACT
    - EMPTY_BUCKET
    - REDSTONE_INTERACT
    - PLAYER_DAMAGE_PERSISTENT_ENTITY
    - BLOCK_BREAK
    - FILL_BUCKET
    - ENTITY_INTERACT
    - PLAYER_DAMAGE_ENTITY
  mob_griefing:
    - MONSTER_DAMAGE_TERRAIN
  explosion_damage:
    - EXPLOSION_DAMAGE_TERRAIN
    - EXPLOSION_DAMAGE_ENTITY
  pvp:
    - PLAYER_DAMAGE_PLAYER
  monster_spawning:
    - MONSTER_SPAWN
    - PASSIVE_MOB_SPAWN
    - PLAYER_DAMAGE_MONSTER
  public_interact_access:
    - ENTITY_INTERACT
    - REDSTONE_INTERACT
    - ENDER_PEARL_TELEPORT
    - BLOCK_INTERACT
```
</details>

<details>
<summary>levels.yml</summary>

```yaml
# ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
# ┃    HuskTowns Levels Config   ┃
# ┃    Developed by William278   ┃
# ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
# ┣╸ This file is for configuring town level requirements and rewards
# ┗╸ Config Help: https://william278.net/docs/husktowns/config-files

# The amount of money required to level up towns. The Level 1 cost will be taken to create a town if require_first_level_collateral is enabled in config.yml.
level_money_requirements:
  '1': 2000.0
  '2': 4000.0
  '3': 8000.0
  '4': 16000.0
  '5': 32000.0
  '6': 64000.0
  '7': 128000.0
  '8': 256000.0
  '9': 512000.0
  '10': 1024000.0
  '11': 2048000.0
  '12': 4096000.0
  '13': 8192000.0
  '14': 1.6384E7
  '15': 3.2768E7
  '16': 6.5536E7
  '17': 1.31072E8
  '18': 2.62144E8
  '19': 5.24288E8
  '20': 1.048576E9
# The maximum number of members a town can have at each level
level_member_limits:
  '1': 5
  '2': 10
  '3': 15
  '4': 20
  '5': 25
  '6': 30
  '7': 35
  '8': 40
  '9': 45
  '10': 50
  '11': 55
  '12': 60
  '13': 65
  '14': 70
  '15': 75
  '16': 80
  '17': 85
  '18': 90
  '19': 95
  '20': 100
# The maximum number of claims a town can have at each level
level_claim_limits:
  '1': 6
  '2': 12
  '3': 18
  '4': 24
  '5': 30
  '6': 36
  '7': 42
  '8': 48
  '9': 54
  '10': 60
  '11': 66
  '12': 72
  '13': 78
  '14': 84
  '15': 90
  '16': 96
  '17': 102
  '18': 108
  '19': 114
  '20': 120
# The bonus crop growth rate percentage a town has at each level (e.g. 105 is 5% faster crop growth)
level_crop_growth_rate_bonus:
  '1': 105.0
  '2': 110.0
  '3': 115.0
  '4': 120.0
  '5': 125.0
  '6': 130.0
  '7': 135.0
  '8': 140.0
  '9': 145.0
  '10': 150.0
  '11': 155.0
  '12': 160.0
  '13': 165.0
  '14': 170.0
  '15': 175.0
  '16': 180.0
  '17': 185.0
  '18': 190.0
  '19': 195.0
  '20': 200.0
# The bonus mob spawner rate percentage a town has at each level
level_mob_spawner_rate_bonus:
  '1': 102.5
  '2': 105.0
  '3': 107.5
  '4': 110.0
  '5': 112.5
  '6': 115.0
  '7': 117.5
  '8': 120.0
  '9': 122.5
  '10': 125.0
  '11': 127.5
  '12': 130.0
  '13': 132.5
  '14': 135.0
  '15': 137.5
  '16': 140.0
  '17': 142.5
  '18': 145.0
  '19': 147.5
  '20': 150.0
```
</details>

<details>
<summary>roles.yml</summary>

```yaml
# ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
# ┃  HuskTowns town role config  ┃
# ┃    Developed by William278   ┃
# ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
# ┣╸ This file is for configuring town roles and associated privileges.
# ┣╸ Each role is mapped to a weight, identifying its hierarchical position. Each weight is also mapped to the role name.
# ┣╸ Config Help: https://william278.net/docs/husktowns/config-files
# ┗╸ Documentation: https://william278.net/docs/husktowns/roles

# Map of role weight IDs to display names
names:
  '1': Resident
  '2': Trustee
  '3': Mayor
# Map of role weight IDs to privileges
roles:
  '1':
    - deposit
    - chat
    - claim_plot
    - spawn
  '2':
    - set_farm
    - set_plot
    - manage_plot_members
    - trusted_access
    - unclaim
    - claim
    - set_greeting
    - set_farewell
    - invite
    - set_spawn
    - manage_relations
    - spawn_privacy
    - view_logs
  '3':
    - set_bio
    - evict
    - promote
    - demote
    - withdraw
    - level_up
    - set_rules
    - rename
    - set_color
    - declare_war
```
</details>

<details>
<summary>rules.yml</summary>

```yaml
# ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
# ┃    HuskTowns Rule Presets    ┃
# ┃    Developed by William278   ┃
# ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
# ┣╸ This file is for configuring the default flag rule rulePresets within towns and the public rules outside of towns.
# ┗╸ Config Help: https://william278.net/docs/husktowns/config-files

# Rules for the wilderness (claimable chunks outside of towns)
wilderness_rules:
  public_farm_access: true
  explosion_damage: true
  pvp: true
  monster_spawning: true
  public_build_access: true
  public_interact_access: true
  public_container_access: true
  fire_damage: true
  mob_griefing: true
# Rules for admin claims (created with /admintown claim)
admin_claim_rules:
  public_farm_access: true
  explosion_damage: false
  pvp: false
  monster_spawning: false
  public_build_access: false
  public_interact_access: true
  public_container_access: false
  fire_damage: false
  mob_griefing: false
# Rules for worlds where claims cannot be created (as defined in unclaimable_worlds)
unclaimable_world_rules:
  public_farm_access: true
  explosion_damage: true
  pvp: true
  monster_spawning: true
  public_build_access: true
  public_interact_access: true
  public_container_access: true
  fire_damage: true
  mob_griefing: true
# Default rules when a town is at war (only used during a town war)
wartime_rules:
  public_farm_access: true
  explosion_damage: true
  pvp: true
  monster_spawning: true
  public_build_access: true
  public_interact_access: true
  public_container_access: true
  fire_damage: true
  mob_griefing: true
# The default rules for different town claim types
default_rules:
  # Default rules for normal claims
  claims:
    public_farm_access: false
    explosion_damage: false
    pvp: false
    monster_spawning: true
    public_build_access: false
    public_interact_access: false
    public_container_access: false
    fire_damage: false
    mob_griefing: false
  # Default rules for farm claims
  farms:
    public_farm_access: true
    explosion_damage: false
    pvp: false
    monster_spawning: true
    public_build_access: false
    public_interact_access: false
    public_container_access: false
    fire_damage: false
    mob_griefing: false
  # Default rules for plot claims
  plots:
    public_farm_access: false
    explosion_damage: false
    pvp: false
    monster_spawning: false
    public_build_access: false
    public_interact_access: false
    public_container_access: false
    fire_damage: false
    mob_griefing: false
```
</details>

<details>
<summary>server.yml</summary>

This file is only present if your server uses cross-server mode to run HuskTowns on a proxy network.
```yaml
# ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
# ┃  HuskTowns Server ID config  ┃
# ┃    Developed by William278   ┃
# ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
# ┣╸ This file should contain the ID of this server as defined in your proxy config.
# ┣╸ If you join it using /server alpha, then set it to 'alpha' (case-sensitive)
# ┗╸ You only need to touch this if you're using cross-server mode.

name: beta
```
</details>