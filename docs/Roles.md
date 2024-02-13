You can customize the roles members of a town may hold as well as the privileges within the town each role has. Towns require at least two roles with different weights; a "mayor" role and a default citizen role, otherwise you will encounter errors.

### Important
* If you change the number of roles, you will need to reset your data, as the role weightings stored in the database will become incompatible.
* You can change the privileges at any time in the config, though
* You can also change the role name as you see fit. It's just the weightings which can't change as those are stored in the database.

## Defining roles
Roles are defined in the town `roles.yml` file below, which is a copy of the default setup.
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
  '3': Mayor
  '2': Trustee
  '1': Resident
# Map of role weight IDs to privileges
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
  - declare_war
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
  '1':
  - deposit
  - chat
  - claim_plot
  - spawn
```

</details>

### Privileges
Role privileges are how you specify what rights each role has in your town. The below table is a list of all these privileges and what rights they give each role. If you don't assign a privilege to a role, nobody will be able to perform that action, so make sure they're all assigned.

> ✅ Remember that higher roles inherit privileges from lower-weighted roles.

| Privilege             | Description                                            |
|-----------------------|--------------------------------------------------------|
| `set_bio`             | Update the town bio                                    |
| `evict`               | Evict a town member                                    |
| `promote`             | Promote a town member to a higher role                 |
| `demote`              | Demote a member to a lower role                        |
| `withdraw`            | Withdraw from the coffers to their own bank balance    |
| `level_up`            | Spend money from the coffers to level up the town      |
| `set_rules`           | Update the town claim/flag rule settings               |
| `rename`              | Rename the town                                        |
| `set_color`           | Set the town color                                     |
| `set_farm`            | Make a claimed chunk into a farm                       |
| `set_plot`            | Make a claimed chunk into a plot                       |
| `manage_plot_members` | Add members and managers to a plot                     |
| `manage_relations`    | Manage [[Relations]], if enabled                  |
| `declare_war`         | Declare and manage [[Wars]], if enabled           |
| `trusted_access`      | Build anywhere in the town, including outside of plots |
| `unclaim`             | Remove a claim                                         |
| `claim`               | Create a claim                                         |
| `set_greeting`        | Update the town greeting message                       |
| `set_farewell`        | Update the town farewell message                       |
| `invite`              | Invite a player to the town                            |
| `set_spawn`           | Update the town spawn position                         |
| `spawn_privacy`       | Update the privacy of the town spawn                   |
| `view_logs`           | View the town audit logs                               |
| `deposit`             | Deposit money into the town coffers                    |
| `chat`                | Use the town chat                                      |
| `spawn`               | Teleport to the town spawn if it is private            |
| `claim_plot`          | Claim a vacant town plot with `/town plot claim`       |

Some actions are automatically given to only the mayor&mdash;such as the ability to disband and transfer ownership of a town. Only one player can be the mayor. If you'd like a "co-mayor" setup, it's recommended that you define two roles - one *true* "mayor" with the highest weighting, and a "co-mayor" weighting just below that, and assign that role all the privileges.