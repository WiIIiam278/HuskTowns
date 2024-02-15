HuskTowns provides an API for getting, creating, and editing [[Towns]]. Note that actions carried out through the API will not fire [[API Events]].

This page assumes you have read the general [[API]] introduction and that you have both imported HuskTowns into your project and added it as a dependency.

## Table of contents
* [1. Getting a Town](#1-getting-a-town)
  * [1.1 Getting a user's town & role](#11-getting-a-users-town--role)
  * [1.2 Checking a user's privileges](#12-checking-a-users-privileges)
* [2. Editing a Town](#2-editing-a-town)
* [3. Creating a Town](#3-creating-a-town)
* [4. Deleting a Town](#4-deleting-a-town)

## 1. Getting a Town
* You can get a town with `#getTown(String name)`
* This will return an `Optional<Town>`; a town wrapped with an Optional (that will be empty if no town exists with the supplied `name`)
* A `Town` object has a range of properties and methods for interacting with the town.
* You can also get the `Town` from a `TownClaim` object - see the [[Claims API]] for more information.

<details>
<summary>Example &mdash; Getting a town by name</summary>

```java
void getTownByName(String townName) {
    Optional<Town> town = huskTowns.getTown(townName);
    if (town.isPresent()) {
        // Do something with the town
        Town townObject = town.get();
        System.out.println("Town name: " + townObject.getName());
    } else {
        System.out.println("No town found with the name " + townName);
    }
}
```
</details>

### 1.1 Getting a user's town & role
* You can get a user's town with `#getUserTown(User user)`, which will return an `Optional<Member>` for the town the user is in.
  * If the `Member` is present, you can use `Member#town()` to get the `Town` object
  * If the `OnlineUser` is not in a town, the `Optional` will be empty (`Optional#isEmpty()`)
* You can then get the user's town `Role` via `Member#role()`

<details>
<summary>Example &mdash; Getting a user's town and role</summary>

```java
void showUserTownAndRole(org.bukkit.Player player) {
    OnlineUser user = huskTowns.getOnlineUser(player);
    Optional<Member> member = huskTowns.getUserTown(user);
    if (member.isPresent()) {
        Town town = member.get().town();
        Role role = member.get().role();
        System.out.println("User " + player.getName() + " is in the town " + town.getName() + " with the role " + role.getName());
    } else {
        System.out.println("User " + player.getName() + " is not in a town");
    }
}
```
</details>

### 1.2 Checking a user's privileges
* You can check what `Privilege`s a `Member` can perform with `HuskTownsAPI#isPrivilegeAllowed(Privilege privilege, User user)`
  * This method will return `true` if the user has the privilege, and `false` if they do not.

<details>
<summary>Example &mdash; Checking if a user can create claims</summary>

```java
void showCanCreateTownClaims(org.bukkit.Player player) {
    boolean allowed = huskTowns.isPrivilegeAllowed(Privilege.CLAIM, huskTowns.getOnlineUser(player));
    if (allowed) {
        System.out.println("User " + player.getName() + " can create claims");
    } else {
        System.out.println("User " + player.getName() + " cannot create claims");
    }
}
```
</details>

## 2. Editing a Town
* You can modify this `Town` object, then save it with `#updateTown(Town town, OnlineUser user)`
* This requires an `OnlineUser` actor to network town changes in cases where the server is using Plugin Message networking in cross-server mode.
  * This user does not have to have permissions to perform the relevant edit actions.
  * You should use a relevant user for the action, such as the player who is performing the action if possible, otherwise a random user.
  * To get an `OnlineUser` object, use the platform modules &mdash; on the Bukkit platform, use `#getOnlineUser(org.bukkit.Player player)`.
* The `#editTown(OnlineUser actor, String townName, Consumer<Town> editor)` provides a way to modify a town in a single action using a `Consumer<Town>`.
  * You can use this method to add a user to a town via `Town#addMember(UUID, role)` &mdash; just be sure to make sure you don't add users to multiple towns!
  * You can also use this method to modify the town's properties, such as the greeting and farewell messages. Be sure to check the Javadoc comments to make sure you properly validate user input.
* This is not the API for creating claims! See the [[Claims API]] for that.

<details>
<summary>Example &mdash; Editing a town</summary>

```java
void editTown(String townName, org.bukkit.Player player) {
    OnlineUser online = huskTowns.getOnlineUser(player);
    huskTowns.editTown(online, townName, town -> {
        town.setGreeting("Welcome to our town!");
        town.setFarewell("Goodbye!");
    });
}
```
</details>

## 3. Creating a Town
* You can create a town with `#createTown(String name, OnlineUser user)`
* This returns a `CompletableFuture<Town>` which will be completed with the new town if it was created successfully.
* This future may complete exceptionally if the town could not be created, for example if a town with the same name already exists; you should handle this case.

<details>
<summary>Example &mdash; Creating a town</summary>

```java
void createTown(String townName, org.bukkit.Player player) {
    OnlineUser online = huskTowns.getOnlineUser(player);
    CompletableFuture<Town> future = huskTowns.createTown(townName, online);
    future.thenAccept(town -> {
        System.out.println("Town created: " + town.getName());
    }).exceptionally(throwable -> {
        System.out.println("Failed to create town: " + throwable.getMessage());
        return null;
    });
}
```
</details>

## 4. Deleting a Town
* You can delete a town with `#deleteTown(String name, OnlineUser user)`
* This will also delete all claims and data associated with the town.

<details>
<summary>Example &mdash; Deleting a town</summary>

```java
void deleteTown(String townName, org.bukkit.Player player) {
    OnlineUser online = huskTowns.getOnlineUser(player);
    huskTowns.deleteTown(townName, online);
}
```
</details>