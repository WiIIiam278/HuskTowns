This page contains the configuration structure for HuskTowns.

## Configuration structure
📁 `plugins/HuskTowns/`
  - 📄 `config.yml`: General plugin configuration
  - 📄 `flags.yml`: Flag definition configuration
  - 📄 `levels.yml`: Town level requirements and limits
  - 📄 [`roles.yml`](town-roles): Town role hierarchy (see [[Town Roles]])
  - 📄 `rules.yml`: Default town/wilderness claim/flag rules
  - 📄 `server.yml`: (Cross-server setups only) Server ID configuration
  - 📄 [`advancements.json`](town-advancements): Town advancements JSON file (see [[Town Advancements]])
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
database:
  # Type of database to use (SQLITE, MYSQL or MARIADB)
  type: SQLITE
  mysql:
    credentials:
      # Specify credentials here if you are using MYSQL or MARIADB as your database type
      host: localhost
      port: 3306
      database: HuskTowns
      username: root
      password: pa55w0rd
      parameters: ?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    connection_pool:
      # MYSQL database Hikari connection pool properties. Don't modify this unless you know what you're doing!
      size: 10
      idle: 10
      lifetime: 1800000
      keepalive: 30000
      timeout: 20000
  # Names of tables to use on your database. Don't modify this unless you know what you're doing!
  table_names:
    user_data: husktowns_users
    town_data: husktowns_town_data
    claim_data: husktowns_claim_worlds
cross_server:
  # Synchronise towns across a proxy network. Requires MySQL. Don't forget to update server.yml
  enabled: false
  # The type of message broker to use for cross-server communication. Options: PLUGIN_MESSAGE, REDIS
  messenger_type: PLUGIN_MESSAGE
  # Specify a common ID for grouping servers running HuskTowns on your proxy. Don't modify this unless you know what you're doing!
  cluster_id: main
  redis:
    # Specify credentials here if you are using REDIS as your messenger_type. Docs: https://william278.net/docs/husktowns/redis-support/
    host: localhost
    port: 6379
    password: ''
    ssl: false
general:
  # How many items should be displayed per-page in chat menu lists
  list_items_per_page: 6
  # Which item to use for the inspector tool; the item that displays claim information when right-clicked.
  inspector_tool: minecraft:stick
  # How far away the inspector tool can be used from a claim. (blocks)
  max_inspection_distance: 80
  # The slot to display claim entry/teleportation notifications in. (ACTION_BAR, CHAT, TITLE, SUBTITLE, NONE)
  notification_slot: ACTION_BAR
  # The width and height of the claim map displayed in chat when runnign the /town map command.
  claim_map_width: 9
  claim_map_height: 9
  # Whether town spawns should be automatically created when a town's first claim is made.
  first_claim_auto_setspawn: false
  # Whether to provide modern, rich TAB suggestions for commands (if available)
  brigadier_tab_completion: true
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
  # Adds special advancements for town progression. Docs: https://william278.net/docs/husktowns/town-advancements/
  do_advancements: true
  # Enable economy features. Requires Vault
  economy_hook: true
  # Hook with LuckPerms to provide town permission contexts. Docs: https://william278.net/docs/husktowns/luckperms-contexts
  luckperms_contexts_hook: true
  # Hook with PlaceholderAPI to provide placeholders. Docs: https://william278.net/docs/husktowns/placeholders
  placeholderapi_hook: true
  # Use HuskHomes for improved teleportation
  huskhomes_hook: true
  # Show town information on your Player Analytics web panel
  plan_hook: true
  web_map_hook:
    # Show claims on your server Dynmap or BlueMap. Docs: https://william278.net/docs/husktowns/map-hooks/
    enabled: true
    # The name of the marker set to use for claims on your web map
    marker_set_name: Claims
towns:
  # Town settings. Check rules.yml, roles.yml and levels.yml for more settings
  allow_unicode_names: false
  # Whether town bios, greetings and farewell messages should be allowed to contain UTF-8 characters
  allow_unicode_bios: true
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
  admin_town:
    # Admin Town settings for changing how admin claims look
    name: Admin
    color: '#ff0000'
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
# A map of flag IDs to allowed operations
flags:
  public_container_access:
  - CONTAINER_OPEN
  fire_damage:
  - FIRE_SPREAD
  - FIRE_BURN
  public_farm_access:
  - BLOCK_INTERACT
  - FARM_BLOCK_PLACE
  - FARM_BLOCK_INTERACT
  - FARM_BLOCK_BREAK
  - PLAYER_DAMAGE_ENTITY
  public_build_access:
  - CONTAINER_OPEN
  - REDSTONE_INTERACT
  - PLAYER_DAMAGE_ENTITY
  - BLOCK_BREAK
  - PLAYER_DAMAGE_MONSTER
  - USE_SPAWN_EGG
  - ENTITY_INTERACT
  - BLOCK_PLACE
  - PLAYER_DAMAGE_PERSISTENT_ENTITY
  - BLOCK_INTERACT
  - PLACE_HANGING_ENTITY
  - FARM_BLOCK_PLACE
  - FARM_BLOCK_INTERACT
  - BREAK_HANGING_ENTITY
  - EMPTY_BUCKET
  - FILL_BUCKET
  mob_griefing:
  - MONSTER_DAMAGE_TERRAIN
  explosion_damage:
  - EXPLOSION_DAMAGE_TERRAIN
  - EXPLOSION_DAMAGE_ENTITY
  pvp:
  - PLAYER_DAMAGE_PLAYER
  monster_spawning:
  - MONSTER_SPAWN
  - PLAYER_DAMAGE_MONSTER
  public_interact_access:
  - BLOCK_INTERACT
  - REDSTONE_INTERACT
  - ENTITY_INTERACT
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
  '14': !!float '1.6384E7'
  '15': !!float '3.2768E7'
  '16': !!float '6.5536E7'
  '17': !!float '1.31072E8'
  '18': !!float '2.62144E8'
  '19': !!float '5.24288E8'
  '20': !!float '1.048576E9'
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
# ┗╸ Documentation: https://william278.net/docs/husktowns/town-roles
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
```
</details>

<details>
<summary>rules.yml</summary>

```yaml
# ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
# ┃    HuskTowns Rule Presets    ┃
# ┃    Developed by William278   ┃
# ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
# ┣╸ This file is for configuring the default flag rule presets within towns and the public rules outside of towns.
# ┗╸ Config Help: https://william278.net/docs/husktowns/config-files
# Rules for the wilderness (claimable chunks outside of towns)
wilderness_rules:
  fire_damage: true
  mob_griefing: true
  public_farm_access: true
  explosion_damage: true
  pvp: true
  monster_spawning: true
  public_build_access: true
  public_interact_access: true
  public_container_access: true
# Rules for admin claims (created with /admintown claim)
admin_claim_rules:
  fire_damage: false
  mob_griefing: false
  public_farm_access: true
  explosion_damage: false
  pvp: false
  monster_spawning: false
  public_build_access: false
  public_interact_access: true
  public_container_access: false
# Rules for worlds where claims cannot be created (as defined in unclaimable_worlds)
unclaimable_world_rules:
  fire_damage: true
  mob_griefing: true
  public_farm_access: true
  explosion_damage: true
  pvp: true
  monster_spawning: true
  public_build_access: true
  public_interact_access: true
  public_container_access: true
default_rules:
  # Default rules for normal claims
  claims:
    fire_damage: false
    mob_griefing: false
    public_farm_access: false
    explosion_damage: false
    pvp: false
    monster_spawning: true
    public_build_access: false
    public_interact_access: false
    public_container_access: false
  # Default rules for farm claims
  farms:
    fire_damage: false
    mob_griefing: false
    public_farm_access: true
    explosion_damage: false
    pvp: false
    monster_spawning: true
    public_build_access: false
    public_interact_access: false
    public_container_access: false
  # Default rules for plot claims
  plots:
    fire_damage: false
    mob_griefing: false
    public_farm_access: false
    explosion_damage: false
    pvp: false
    monster_spawning: false
    public_build_access: false
    public_interact_access: false
    public_container_access: false
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