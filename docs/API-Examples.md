HuskTowns provides a range of methods for getting, deleting and updating claims and town data.

This page assumes you've read the [[API]] introduction and have imported the HuskTowns API into your repository.

## Project setup
### Creating a class to interface with the API
- Unless your plugin completely relies on HuskTowns, you shouldn't put HuskTowns API calls into your main class, otherwise if HuskTowns is not installed you'll encounter `ClassNotFoundException`s

<details>
<summary>Creating a hook class</summary>

```java
public class HuskTownsAPIHook {

    public HuskTownsAPIHook() {
        // Ready to do stuff with the API
    }

}
```
</details>

### Checking if HuskTowns is present and creating the hook
- Check to make sure the HuskTowns plugin is present before instantiating the API hook class

<details>
<summary>Instantiating your hook</summary>

```java
public class MyPlugin extends JavaPlugin {

    public HuskTownsAPIHook huskTownsHook;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("HuskTowns") != null) {
            this.huskTownsHook = new HuskTownsAPIHook();
        }
    }
}
```
</details>

### Getting an instance of the API
- You can now get the API instance by calling `HuskTownsAPI#getInstance()`

<details>
<summary>Getting an API instance</summary>

```java
import net.william278.husktowns.api.BukkitHuskTownsAPI;

public class HuskTownsAPIHook {

    private final HuskTownsAPI huskTownsAPI;

    public HuskTownsAPIHook() {
        this.huskTownsAPI = HuskTownsAPI.getInstance();
    }

}
```
</details>

(Documentation forthcoming...)