> ⚠️ **API v1 is no longer supported by HuskTowns v3**. Please refer to the new HuskTowns [[API]] v3 for the current API specification.

The HuskTowns API v1 provides methods to get data from HuskTowns directly. API v1 has been deprecated and superseded by the HuskTownsAPI v3 (See the new [[API]] documentation for more information).

The API accesses cached data and can be used to check for things such as players being able to build on certain chunks, etc. This page contains how to use the API and provides example usages for developers.

## Table of contents
1. Adding the API to your project
   1. Setup with Maven
   2. Setup with Gradle
   3. Adding a dependency
   4. Getting an instance
2. API Examples

## 1. API Introduction
### 1.1 Setup with Maven
<details>
<summary>Maven setup information</summary>

- Add the repository to your `pom.xml` as per below.
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
- Add the dependency to your `pom.xml` as per below.
```xml
<dependency>
    <groupId>net.william278</groupId>
    <artifactId>HuskTowns</artifactId>
    <version>1.8.2</version>
    <scope>provided</scope>
</dependency>
```
</details>

### 1.2 Setup with Gradle
<details>
<summary>Gradle setup information</summary>

- Add the dependency like so to your `build.gradle`:
```groovy
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}
```
- Add the dependency as per below.

```groovy
dependencies {
    compileOnly 'net.william278:HuskTowns:1.8.2'
}
```
</details>

### 1.3 Adding HuskTowns as a dependency
- Add HuskTowns to your `softdepend` (if you want to optionally use HuskTowns) or `depend` (if your plugin relies on HuskTowns) section in `plugin.yml` of your project.

```yaml
name: MyPlugin
version: 1.0
main: net.william278.myplugin.MyPlugin
author: William278
description: 'A plugin that hooks with the HuskTowns v1 API!'
softdepend: # Or, use 'depend' here
  - HuskTowns
```

### 1.4. Getting an instance of the API
Once you have added the API dependency, you can get an instance of it using `HuskTownsAPI.getInstance()`. This is the entrypoint for utilising the various methods, which you can look at on the [Javadoc](https://javadoc.jitpack.io/com/github/WiIIiam278/HuskTowns/husktowns/1.8.2/javadoc/).

## 2. API Examples
### 2.1 Check if a location or block is in the wilderness

#### Method
```java
/**
* Check if the specified {@link Block} is in the wilderness (outside of a claim).
* @param block {@link Block} to check.
* @return true if the {@link Block} is in the wilderness; otherwise return false.
*/
boolean isWilderness = HuskTownsAPI.getInstance().isWilderness(Block block);
```
or
```java
/**
* Check if the specified {@link Location} is in the wilderness (outside of a claim).
* @param location {@link Location} to check.
* @return true if the {@link Location} is in the wilderness; otherwise return false.
*/
boolean isWilderness = HuskTownsAPI.getInstance().isWilderness(Location location);
```

#### Example
```java
Location location = player.getLocation();
if (isWilderness(location)) {
  player.sendMessage("In wilderness");
} else {
  player.sendMessage("In a claim");
}
```

### 2.2 Get the name of a town at a location

#### Method
```java
/**
* Returns the name of the town at the specified {@link Location}.
* @param location {@link Location} to check.
* @return the name of the town who has a claim at the specified {@link Location}; null if there is no claim there.
*/
String town = HuskTownsAPI.getInstance().getTownAt(Location location);
```

#### Example
```java
String townName = HuskTownsAPI.getInstance().getTownAt(player.getLocation());
if (townName == null) {
  player.sendMessage("In wilderness")
} else {
  player.sendMessage("You're standing in " + townName)
}
```

### 2.3 Get whether a player is in a town

#### Method
```java
/**
* Returns true if the {@link Player} is in a town; false if not.
* @param player {@link Player} to check.
* @return true if the {@link Player} is in a town; false otherwise.
*/
boolean inTown = HuskTownsAPI.getInstance().isInTown(Player player);
```

#### Example
```java
boolean inTown = HuskTownsAPI.getInstance().isInTown(player);
if (inTown) {
  player.sendMessage("You're a member of a town")
} else {
  player.sendMessage("You're not a member of a town")
}
```

### 2.4 Get the name of a town a player is in

#### Method
```java
/**
* Returns the name of the town the {@link Player} is currently in; null if they are not in a town
* @param player {@link Player} to check.
* @return the name of the town the {@link Player} is currently in; null if they are not in a town.
*/
String town = HuskTownsAPI.getInstance().getPlayerTown(Player player);
```

#### Example
```java
String townName = HuskTownsAPI.getInstance().getPlayerTown(player);
if (townName == null) {
  player.sendMessage("You're not in a town")
} else {
  player.sendMessage("You're a member of " + townName)
}
```

### 2.5 Get whether the player can build at a location. There are also methods for checking if a player can open containers / interact with the environment.

#### Method
```java
/**
* Returns whether or not the specified {@link Player} can build at the specified {@link Location}.
* @param player {@link Player} to check.
* @param location {@link Location} to check.
* @return true if the player can build at the specified {@link Location}; false otherwise.
*/
boolean canBuild = HuskTownsAPI.getInstance().canBuild(Player player, Location location);
```

#### Example
```java
boolean canBuild = HuskTownsAPI.getInstance().canBuild(player, player.getLocation());
if (canBuild) {
  player.sendMessage("You can build here!")
} else {
  player.sendMessage("You don't have access to build here.")
}
```

### 2.6 Get a list of all the town names
#### Method
```java
/**
* Get a list of the names of all towns
* @return A HashSet of all town names
*/
HashSet<String>  towns = HuskTownsAPI.getInstance().getTowns();
```

#### Example
```java
HashSet<String>  towns = HuskTownsAPI.getInstance().getTowns();
StringJoiner joiner = new StringJoiner(", ");
for (String townName : towns) {
  joiner.add(townName);
}
player.sendMessage("Towns on the server: " + joiner.toString());
```

### 2.7 Get a list of all the town names who have a public spawn
#### Method
```java
/**
* Get a list of the names of all towns who have their town spawn set to public
* @return A HashSet of the names of all towns with their spawn set to public
*/
HashSet<String>  publicSpawnTowns = HuskTownsAPI.getInstance().getTownsWithPublicSpawn();
```

#### Example
```java
HashSet<String>  publicSpawnTowns = HuskTownsAPI.getInstance().getTownsWithPublicSpawn();
StringJoiner joiner = new StringJoiner(", ");
for (String townName : publicSpawnTowns ) {
  joiner.add(townName);
}
player.sendMessage("Towns you can build in: " + joiner.toString());
```

### 2.8 Get town bio / greeting / farewell message
#### Methods
```java
/**
* Returns the message sent to players when they enter a town's claim
* @param townName The name of the town
* @return The town's greeting message.
*/
String  welcomeMessage = HuskTownsAPI.getInstance().getTownGreetingMessage(String townName);

/**
* Returns the message sent to players when they leave a town's claim
* @param townName The name of the town
* @return The town's farewell message.
*/
String  farewellMessage = HuskTownsAPI.getInstance().getTownFarewellMessage(String townName);

/**
* Returns the bio of a town
* @param townName The name of the town
* @return The town's bio.
*/
String  bio = HuskTownsAPI.getInstance().getTownBio(String townName);
```

#### Example
```java
HuskTownsAPI huskTownsAPI = HuskTownsAPI.getInstance();
Player player = e.getPlayer();

if (huskTownsAPI.isInTown(player) {
  final String townName = huskTownsAPI.getPlayerTown(player)
  player.sendMessage("You are in the town, " + townName);
  player.sendMessage(huskTownsAPI.getBio("Bio: " + townName));
  player.sendMessage(huskTownsAPI.getWelcomeMessage("Greeting message: " + townName));
  player.sendMessage(huskTownsAPI.getFarewellMessage("Farewell message: " + townName));
} else {
  player.sendMessage("You are not in a town!");
}
```