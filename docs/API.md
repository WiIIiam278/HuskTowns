The HuskTowns API provides methods for interfacing and editing towns, claims and users, alongside a selection of API events for listening to when players perform certain town actions.

## Compatibility
[![Maven](https://repo.william278.net/api/badge/latest/releases/net/william278/husktowns/husktowns-common?color=00fb9a&name=Maven&prefix=v)](https://repo.william278.net/#/releases/net/william278/husktowns/)

The HuskTowns API shares version numbering with the plugin itself for consistency and convenience. Please note minor and patch plugin releases may make API additions and deprecations, but will not introduce breaking changes without notice.

| API Version |  HuskTowns Versions  | Supported |
|:-----------:|:--------------------:|:---------:|
|    v3.x     | _v3.0&mdash;Current_ |     ✅     |
|    v2.x     | _v2.0&mdash;v2.3.1_  |     ❌     |
|    v1.x     | _v1.0&mdash;v2.3.1_  |     ❌     |

### Platforms
> **Note:** For versions older than `v3.0`, the HuskTowns API was only distributed for the Bukkit platform (as `net.william278:husktowns`)

The HuskTowns API is available for the following platforms:

* `bukkit` - Bukkit, Spigot, Paper, etc. Provides Bukkit API event listeners and adapters to `org.bukkit` objects.
* `common` - Common API for all platforms.

<details>
<summary>Targeting older versions</summary>

* The HuskTowns API was only distributed for the Bukkit module prior to `v3.0`; the artifact ID was `net.william278:husktowns` instead of `net.william278.husktowns:husktowns-PLATFORM`.
* HuskTowns versions prior to `v2.3.1` are distributed on [JitPack](https://jitpack.io/#/net/william278/HuskTowns), and you will need to use the `https://jitpack.io` repository instead.</details>
</details>

## Table of Contents
1. [API Introduction](#api-introduction)
    1. [Setup with Maven](#11-setup-with-maven)
    2. [Setup with Gradle](#12-setup-with-gradle)
2. [Creating a class to interface with the API](#3-creating-a-class-to-interface-with-the-api)
3. [Checking if HuskTowns is present and creating the hook](#4-checking-if-husktowns-is-present-and-creating-the-hook)
4. [Getting an instance of the API](#5-getting-an-instance-of-the-api)
5. [CompletableFuture and Optional basics](#6-completablefuture-and-optional-basics)
6. [Next steps](#7-next-steps)

## API Introduction
### 1.1 Setup with Maven
<details>
<summary>Maven setup information</summary>

Add the repository to your `pom.xml` as per below. You can alternatively specify `/snapshots` for the repository containing the latest development builds (not recommended).
```xml
<repositories>
    <repository>
        <id>william278.net</id>
        <url>https://repo.william278.net/releases</url>
    </repository>
</repositories>
```
Add the dependency to your `pom.xml` as per below. Replace `VERSION` with the latest version of HuskTowns (without the v): ![Latest version](https://img.shields.io/github/v/tag/WiIIiam278/HuskTowns?color=%23282828&label=%20&style=flat-square)
```xml
<dependency>
    <groupId>net.william278.husktowns</groupId>
    <artifactId>husktowns-PLATFORM</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```
</details>

### 1.2 Setup with Gradle
<details>
<summary>Gradle setup information</summary>

Add the dependency as per below to your `build.gradle`. You can alternatively specify `/snapshots` for the repository containing the latest development builds (not recommended).
```groovy
allprojects {
	repositories {
		maven { url 'https://repo.william278.net/releases' }
	}
}
```
Add the dependency as per below. Replace `VERSION` with the latest version of HuskTowns (without the v): ![Latest version](https://img.shields.io/github/v/tag/WiIIiam278/HuskTowns?color=%23282828&label=%20&style=flat-square)

```groovy
dependencies {
    compileOnly 'net.william278.husktowns:husktowns-PLATFORM:VERSION'
}
```
</details>

### 2. Adding HuskTowns as a dependency
Add HuskTowns to your `softdepend` (if you want to optionally use HuskTowns) or `depend` (if your plugin relies on HuskTowns) section in `plugin.yml` of your project.

```yaml
name: MyPlugin
version: 1.0
main: net.william278.myplugin.MyPlugin
author: William278
description: 'A plugin that hooks with the HuskTowns API!'
softdepend: # Or, use 'depend' here
  - HuskTowns
```

## 3. Creating a class to interface with the API
- Unless your plugin completely relies on HuskTowns, you shouldn't put HuskTowns API calls into your main class, otherwise if HuskTowns is not installed you'll encounter `ClassNotFoundException`s

```java
public class HuskTownsAPIHook {

    public HuskTownsAPIHook() {
        // Ready to do stuff with the API
    }

}
```
## 4. Checking if HuskTowns is present and creating the hook
- Check to make sure the HuskTowns plugin is present before instantiating the API hook class

```java
public class MyPlugin extends JavaPlugin {

    public HuskTownsAPIHook huskTownsAPIHook;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("HuskTowns") != null) {
            this.huskTownsAPIHook = new HuskTownsAPIHook();
        }
    }
}
```

## 5. Getting an instance of the API
- You can now get the API instance by calling `HuskTownsAPI#getInstance()`
- If targeting the Bukkit platform, you can also use `BukkitHuskTownsAPI#getBukkitInstance()` to get the Bukkit-extended API instance (recommended)

```java
import net.william278.husktowns.api.HuskTownsAPI;

public class HuskTownsAPIHook {

    private final HuskTownsAPI huskTownsAPI;

    public HuskTownsAPIHook() {
        this.huskTownsAPI = HuskTownsAPI.getInstance();
    }

}
```

## 6. CompletableFuture and Optional basics
- HuskTowns API methods often deal with `CompletableFuture`s and `Optional`s.
- A `CompletableFuture` is an asynchronous callback mechanism. The method will be processed asynchronously and the data returned when it has been retrieved. Then, use `CompletableFuture#thenAccept(data -> {})` to do what you want to do with the `data` you requested after it has asynchronously been retrieved, to prevent lag.
- An `Optional` is a null-safe representation of data, or no data. You can check if the Optional is empty via `Optional#isEmpty()` (which will be returned by the API if no data could be found for the call you made). If the optional does contain data, you can get it via `Optional#get().

> **Warning:** You should never call `#join()` on futures returned from the HuskTownsAPI as futures are processed on server asynchronous tasks, which could lead to thread deadlock and crash your server if you attempt to lock the main thread to process them.

### 7. Next steps
Now that you've got everything ready, you can start doing stuff with the HuskTowns API!
- [[Towns API]]
- [[Claims API]]
- [[API Events]]
