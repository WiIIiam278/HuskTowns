This page contains the configuration structure for HuskTowns.

## Configuration structure
📁 `plugins/HuskTowns/`
  - 📄 `config.yml`: General plugin configuration
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
# ┗╸ Documentation: https://william278.net/docs/husktowns
language: en-gb
check_for_updates: true
database:
  # Database connection settings
  type: SQLITE
  mysql:
    credentials:
      host: localhost
      port: 3306
      database: HuskTowns
      username: root
      password: pa55w0rd
      parameters: ?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    connection_pool:
      # MySQL connection pool properties
      size: 10
      idle: 10
      lifetime: 1800000
      keepalive: 30000
      timeout: 20000
  table_names:
    town_data: husktowns_town_data
    claim_data: husktowns_claim_worlds
    user_data: husktowns_users
cross_server:
  # Synchronise towns across a proxy network. Requires MySQL. Don't forget to update server.yml
  enabled: false
  messenger_type: PLUGIN_MESSAGE
  # Sub-network cluster identifier. Don't edit this unless you know what you're doing
  cluster_id: main
  redis:
    # Redis connection properties
    host: localhost
    port: 6379
    password: ''
    ssl: false
general:
  # General system settings
  list_items_per_page: 6
  inspector_tool: minecraft:stick
  max_inspection_distance: 80
  # The slot to display claim entry/teleportation notifications in. (ACTION_BAR, CHAT, TITLE, SUBTITLE, NONE)
  notification_slot: ACTION_BAR
  claim_map_width: 9
  claim_map_height: 9
  first_claim_auto_setspawn: false
  brigadier_tab_completion: true
  allow_friendly_fire: false
  unclaimable_worlds:
  - world_nether
  - world_the_end
  prohibited_town_names:
  - Administrators
  - Moderators
  - Mods
  - Staff
  - Server
  # Add special advancements for town progression to your server
  do_advancements: true
  # Enable economy features. Requires Vault or RedisEconomy
  economy_hook: true
  # Provide permission contexts via LuckPerms
  luckperms_contexts_hook: true
  # Use PlaceholderAPI for placeholders
  placeholderapi_hook: true
  # Use HuskHomes for improved teleportation
  huskhomes_hook: true
  # Show town information on your Player Analytics web panel
  plan_hook: true
  web_map_hook:
    # Show claims on your server Dynmap or BlueMap
    enabled: true
    marker_set_name: Claims
towns:
  # Town settings. Check rules.yml, roles.yml and levels.yml for more settings
  allow_unicode_names: false
  allow_unicode_bios: true
  # Require the level 1 cost as collateral when creating a town (this cost is otherwise ignored)
  require_first_level_collateral: false
  # The minimum distance apart towns must be, in chunks
  minimum_chunk_separation: 0
  # Require towns to have all their claims adjacent to each other
  require_claim_adjacency: false
  admin_town:
    # Admin Town settings for changing how admin claims look
    name: Admin
    color: '#ff0000'
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
# ┗╸ Documentation: https://william278.net/docs/husktowns/claim-rules
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
level_crop_growth_rate_bonus:
  '1': -0.85
  '2': -0.7
  '3': -0.55
  '4': -0.4
  '5': -0.25
  '6': -0.10000000000000009
  '7': 0.050000000000000044
  '8': 0.19999999999999996
  '9': 0.34999999999999987
  '10': 0.5
  '11': 0.6499999999999999
  '12': 0.7999999999999998
  '13': 0.95
  '14': 1.1
  '15': 1.25
  '16': 1.4
  '17': 1.5499999999999998
  '18': 1.6999999999999997
  '19': 1.85
  '20': 2.0
level_mob_spawner_rate_bonus:
  '1': -0.9
  '2': -0.8
  '3': -0.7
  '4': -0.6
  '5': -0.5
  '6': -0.3999999999999999
  '7': -0.29999999999999993
  '8': -0.19999999999999996
  '9': -0.09999999999999998
  '10': 0.0
  '11': 0.10000000000000009
  '12': 0.20000000000000018
  '13': 0.30000000000000004
  '14': 0.40000000000000013
  '15': 0.5
  '16': 0.6000000000000001
  '17': 0.7000000000000002
  '18': 0.8
  '19': 0.9000000000000001
  '20': 1.0
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
# ┗╸ Documentation: https://william278.net/docs/husktowns/town-roles
names:
  '3': Mayor
  '2': Trustee
  '1': Resident
roles:
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
  '1':
  - deposit
  - chat
  - spawn
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
# ┗╸ Documentation: https://william278.net/docs/husktowns/claim-rules
wilderness_rules:
  monster_spawning: true
  public_build_access: true
  public_interact_access: true
  public_container_access: true
  fire_damage: true
  mob_griefing: true
  public_farm_access: true
  explosion_damage: true
  pvp: true
admin_claim_rules:
  monster_spawning: false
  public_build_access: false
  public_interact_access: true
  public_container_access: false
  fire_damage: false
  mob_griefing: false
  public_farm_access: true
  explosion_damage: false
  pvp: false
unclaimable_world_rules:
  monster_spawning: true
  public_build_access: true
  public_interact_access: true
  public_container_access: true
  fire_damage: true
  mob_griefing: true
  public_farm_access: true
  explosion_damage: true
  pvp: true
default_rules:
  claims:
    monster_spawning: true
    public_build_access: false
    public_interact_access: false
    public_container_access: false
    fire_damage: false
    mob_griefing: false
    public_farm_access: false
    explosion_damage: false
    pvp: false
  farms:
    monster_spawning: true
    public_build_access: false
    public_interact_access: false
    public_container_access: false
    fire_damage: false
    mob_griefing: false
    public_farm_access: true
    explosion_damage: false
    pvp: false
  plots:
    monster_spawning: false
    public_build_access: false
    public_interact_access: false
    public_container_access: false
    fire_damage: false
    mob_griefing: false
    public_farm_access: false
    explosion_damage: false
    pvp: false
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