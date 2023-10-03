HuskTowns provides several API events your plugin can listen to when players do certain town-related things. These events deal in HuskTowns class types, so you may want to familiarize yourself with the [API basics](API) first. Note that on cross-server setups, events only fire on the *server the event occurred on* and will not fire as a result of API calls/updates.

Consult the Javadocs for more information&mdash;and don't forget to register your listener when listening for these event calls.

## List of API Events
| Bukkit Event class      | Since | Cancellable | Description                                                                    |
|-------------------------|:-----:|:-----------:|--------------------------------------------------------------------------------|
| `TownCreateEvent`       |  1.8  |      ✅      | Called when a town is created                                                  |
| `PostTownCreateEvent`   |  2.6  |      ❌      | Called after a town is created                                                 |
| `TownDisbandEvent`      |  1.8  |      ✅      | Called when a town is deleted                                                  |
| `ClaimEvent`            |  1.8  |      ✅      | Called when a player claims a chunk for a town                                 |
| `UnClaimEvent`          |  1.8  |      ✅      | Called when a player deletes a claim                                           |
| `UnClaimAllEvent`       |  2.1  |      ✅      | Called when a player deletes all of a town's claims                            |
| `PlayerEnterTownEvent`  |  2.0  |      ✅      | Called when a player walks into a town claim from wilderness or another town   |
| `PlayerLeaveTownEvent`  |  2.0  |      ✅      | Called when a player walks out of a town claim into wilderness or another town |
| `MemberJoinEvent`       |  2.0  |      ✅      | Called when a player joins a town                                              |
| `MemberLeaveEvent`      |  2.0  |      ✅      | Called when a player leaves or is evicted from a town                          |
| `MemberRoleChangeEvent` |  2.0  |      ✅      | Called when a player is promoted or demoted within a town                      |
