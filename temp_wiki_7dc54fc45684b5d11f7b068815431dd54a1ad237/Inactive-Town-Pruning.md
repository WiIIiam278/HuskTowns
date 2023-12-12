HuskTowns (v2.4+) provides an option to prune towns based on inactivity; towns where no members have logged on in a configurable amount of time can be deleted, either automatically or through the `/admintown prune <time>` command.

## Using /admintown prune
The `/admintown prune <time> [confirm]` command can be used to delete towns that have been inactive for a certain amount of time. The time is specified in the format `<number><unit>`, where `<number>` is the number of `<unit>`s of time. For example, `1d` would be one day, `2w` would be two weeks, and `3m` would be three months. If you don't specify a unit, days will be used. The following units are supported:
- `d`: days
- `w`: weeks
- `m`: months
- `y`: years

Towns where no members have logged on in the specified amount of time will be deleted, along with all associated claims.

When executing the command, you will be prompted to confirm deletion in chat. The number of affected towns will be displayed in the chat message. If you want to skip the confirmation, you can add the `confirm` argument to the command.

## Automatically prune on startup
HuskTowns can automatically prune towns that have been inactive for a certain amount of time. To enable this, set the `prune_on_startup` setting to `true` under `town_pruning`, and configure a number of days after which inactive towns should be pruned by modifying the `prune_after_days` setting (minimum: 1 day). When the server starts, HuskTowns will automatically prune towns that have been inactive for the specified amount of time.

<details>
<summary>Example config</summary>

```yaml
town_pruning:
  prune_on_startup: true
  prune_after_days: 90
```
</details>