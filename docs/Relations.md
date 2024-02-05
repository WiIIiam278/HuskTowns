Town Relations, added in HuskTowns v2.6, allow [[Towns]] to mark each other as allies or enemies.

## 1. Enabling/disabling town relations
The Town Relations feature is enabled by default, and can be disabled by editing your [`config.yml`](config-files).

<details>
<summary>Town Relations (config.yml)</summary>

```yaml
relations:
    # Enable town relations (alliances and enemies). Docs: https://william278.net/docs/husktowns/town-relations/
    enabled: true
```
</details>


## 2. Managing relations
The `/town relations` command allows you to view the current relations of the town you are in. You can use `/town relations list (town)` to view the relations of another town.

If you have the `MANAGE_RELATIONS` privilege in the town you are in, you can use `/town relations set <ally/enemy> <town>` to mark another town as an ally or an enemy. The town will then show up in your relations list.

### 2.1 Forming alliances
Towns that you have marked as an Ally grant one additional property: If the option to restrict friendly fire is enabled, PvP between two allied towns will not be permitted.

### 2.2 Enemies & War
If you enable [[Wars]] on your server, towns which mark each other as enemies will be able to declare and go to war with each other.

### 2.3 Neutral relations
Towns not marked as an ally or enemy are considered to have a "neutral" relation. In other words, this is your default relation with other towns on the server.

To set a town which you have marked as an ally or enemy as "neutral", use `/town relations set neutral <town>`