If you have [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) installed, HuskTowns (v2.2+) will automatically register a hook providing a number of placeholders that will be replaced with their appropriate values.

## Placeholders
The following table contains the available placeholders. You can customize the replacement messages in the plugin locales file. If you wish to customize the Yes/No messages, please edit your PlaceholderAPI config as those are sourced from there.

### Standard placeholders
| Placeholder                                          | Replacement                      | Notes                                                             |
|------------------------------------------------------|----------------------------------|-------------------------------------------------------------------|
| `%husktowns_town_name%`                              | `Waterside`                      | Name of town the player is in¹                                    |
| `%husktowns_town_role%`                              | `Citizen`                        | Name of the role the player has in their town¹                    |
| `%husktowns_town_mayor%`                             | `William278`                     | Name of the player's town mayor¹                                  |
| `%husktowns_town_color%`                             | `#ff1200`                        | Color of the player's town (gray if not in one)                   |
| `%husktowns_town_members%`                           | `Mark12, William278, MeetThePro` | List of members of the player's town¹                             |
| `%husktowns_town_member_count%`                      | `3`                              | Number of members of the player's town¹                           |
| `%husktowns_town_claim_count%`                       | `4`                              | Number of claims made by the player's town¹                       |
| `%husktowns_town_max_claims%`                        | `6`                              | Maximum number of claims the player's town can make¹              |
| `%husktowns_town_max_members%`                       | `5`                              | Maximum number of members the player's town can have¹             |
| `%husktowns_town_crop_growth_rate%`                  | `105.0`                          | Percentage bonus crop growth rate of the player's town¹           |
| `%husktowns_town_mob_spawner_rate%`                  | `102.5`                          | Percentage bonus mob spawner rate of the player's town¹           |
| `%husktowns_town_money%`                             | `1230.32`                        | Balance of the player's town¹                                     |
| `%husktowns_town_level_up_cost%`                     | `1500.00`                        | Cost for the player's town's next level up¹                       |
| `%husktowns_town_level%`                             | `1`                              | Current level of the player's town¹                               |
| `%husktowns_town_max_level%`                         | `20`                             | Maximum town level                                                |
| `%husktowns_current_location_town%`                  | `Tomoeda`                        | Name of the town who owns the claim the player is in²             |
| `%husktowns_current_location_town_color%`            | `#aaaaaa`                        | Color of the town who owns the claim the player is in⁵            |
| `%husktowns_current_location_can_build%`             | `No`                             | If the player can build in the claim they are in³                 |
| `%husktowns_current_location_can_interact%`          | `Yes`                            | If the player can interact in the claim they are in³              |
| `%husktowns_current_location_can_open_containers%`   | `No`                             | If the player can open containers in the claim they are in³       |
| `%husktowns_current_location_claim_type%`            | `Plot`                           | The type of claim they are in (`Claim`, `Plot`, `Farm`)³          |
| `%husktowns_current_location_plot_members%`          | `Sakura32, Toya567`              | If this is a plot, the name of members of the plot⁴               |
| `%husktowns_current_location_plot_managers%`         | `Sakura32`                       | If this is a plot, the name of managers of the plot⁴              |
| `%husktowns_current_location_town_money%`            | `1130.50`                        | Balance of the town who owns the claim the player is in³          |
| `%husktowns_current_location_town_level%`            | `3`                              | Level of the town who owns the claim the player is in³            |
| `%husktowns_current_location_town_level_up_cost%`    | `2400.00`                        | Cost of the town who owns the claim the player is in to level up³ |
| `%husktowns_current_location_town_max_claims%`       | `15`                             | Maximum number of claims of the town the player is in can make³   |
| `%husktowns_current_location_town_max_members%`      | `20`                             | Maximum number of members the town the player is in can have³     |
| `%husktowns_current_location_town_crop_growth_rate%` | `105.0`                          | Percentage bonus crop growth rate of the town the player is in³   |
| `%husktowns_current_location_town_mob_spawner_rate%` | `102.5`                          | Percentage bonus mob spawner rate of the town the player is in³   |

¹ &mdash; Or, `Not in town` if the player is not in a town <br/>
² &mdash; Or, `Wilderness` if they are not standing in a claim <br/>
³ &mdash; Or, `Not in claim` if they are not standing in a claim <br/>
⁴ &mdash; Displays `Not in claim` if they are not standing in a claim and `Not a plot` if they are not in a plot <br/>
⁵ &mdash; Returns a gray color for wilderness (if they are not standing in a claim)

### Leaderboard placeholders
These placeholders return the name of the town dependent on their position in the list of towns, sorted by their respective property (where 1 is the highest ranked town). Useful for making in-game leaderboards to promote competition among towns!

* `%husktowns_town_leaderboard_members_{index}%` &mdash; Most members
* `%husktowns_town_leaderboard_claims_{index}%` &mdash; Most claims
* `%husktowns_town_leaderboard_money_{index}%` &mdash; Highest balance
* `%husktowns_town_leaderboard_level_{index}%` &mdash; Highest level