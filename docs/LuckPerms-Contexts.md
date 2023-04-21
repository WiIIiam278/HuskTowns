HuskTowns provides additional [Permission Contexts](https://luckperms.net/wiki/Context) for [LuckPerms](https://luckperms.net), letting you restrict permission access based on contextual factors determined by HuskTowns. 

If you wish to, for instance, restrict certain commands behind a player being in a town, this is how you can do so.

## Requirements
* LuckPerms v5.3+ installed on your server
* HuskTowns installed on your server
* In HuskTowns' config.yml, ensure `luckperms_contexts_hook` is enabled under `general`.

## HuskTowns-provided contexts
> ✅ See the [LuckPerms Wiki](https://luckperms.net/wiki/Context) for how to make use of contexts.

| Context                          | Description                                                                      |
|----------------------------------|----------------------------------------------------------------------------------|
| `husktowns:claim-town`           | Name of the town claiming the player is standing in                              |
| `husktowns:in-claim`             | `true`/`false`; if a player is in a claim or not                                 |
| `husktowns:can-build`            | `true`/`false`; if a player can place and break blocks                           |
| `husktowns:can-open-containers`  | `true`/`false`; if a player can use containers                                   |
| `husktowns:can-interact`         | `true`/`false`; if a player can interact (right click) with blocks and entities  |
| `husktowns:standing-in-own-town` | `true`/`false`; if a player is standing in a claim owned by the town they are in |
| `husktowns:is-town-member`       | `true`/`false`; if a player is a member of a town                                |
| `husktowns:town`                 | Name of the town the player is in                                                |
| `husktowns:town-role`            | The player's role in their town; mayor, resident or trusted                      |
| `husktowns:town-level`           | The level of the town the player is in                                           |