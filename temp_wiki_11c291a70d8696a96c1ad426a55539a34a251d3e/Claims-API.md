HuskTowns provides API for getting, creating, changing the type of, & deleting [[claims]] and admin claims.

This page assumes you have read the general [[API]] introduction and that you have both imported HuskTowns into your project and added it as a dependency.

## Table of contents
1. [Getting if a location is claimed](#1-getting-if-a-location-is-claimed)
    1. [Getting the ClaimWorld for a World](#11-getting-the-claimworld-for-a-world)
2. [Checking what a user can do at a location](#2-checking-what-a-user-can-do-at-a-location)
3. [Creating a claim](#3-creating-a-claim)
    1. [Editing a claim](#31-editing-a-claim)
    2. [Deleting a claim](#32-deleting-a-claim)
4. [Highlighting a claim](#4-highlighting-a-claim)

## 1. Getting if a location is claimed
* On the Bukkit platform, get a `Position` object using `#getPosition(org.bukkit.Location location)`
* Use `#isClaimAt(Position position)` to check if the location has been claimed
* Or, use `#getClaimAt(Position position)` to get the `Optional<TownClaim>` at the location
    * With an `Optional<TownClaim>`, you can use `Optional#isPresent()` to check if a claim exists at the location
    * With a `TownClaim` object, you can get the associated `Town` object (see [[Towns API]]) using `#town()`, and the `Claim` itself using `#claim()`
    * The `Claim` object has a range of properties describing the claim.

<details>
<summary>Example &mdash; Getting if a location is claimed</summary>

```java
void showTownWhoHasClaimedAt(org.bukkit.Location location) {
    Position position = huskTowns.getPosition(location);
    Optional<TownClaim> claim = huskTowns.getClaimAt(position);
    if (claim.isPresent()) {
        System.out.println("This location is claimed by " + claim.get().town().getName());
    }
}
```
</details>

### 1.1 Getting the ClaimWorld for a World
* Claims exist within a `ClaimWorld` in HuskTowns. `World`s without `ClaimWorld`s are not protected by HuskTowns.
* On the Bukkit platform, get a `World` object from a Bukkit World using `#getWorld(org.bukkit.World)` (or call `#getWorld()` on a `Position` object)
* You can then get the `ClaimWorld` for a world using `#getClaimWorld(World world)` which will return an `Optional<ClaimWorld>`

<details>
<summary>Example &mdash; Getting the claim world for a world</summary>

```java
void showClaimWorld(org.bukkit.World world) {
    Optional<ClaimWorld> claimWorld = huskTowns.getClaimWorld(world);
    if (claimWorld.isPresent()) {
        System.out.println("This world is protected by HuskTowns, and contains " + claimWorld.get().getClaimCount() + " claims!");
    }
}
```
</details>

## 2. Checking what a user can do at a location
* On the Bukkit platform, get an `OnlineUser` object using `#getOnlineUser(@NotNull org.bukkit.Player player)`
    * Use `#getPosition()` to get the `Position` of an `OnlineUser` to check if there's a claim where they stand (see #1)
* Check if a user can perform `OperationTypes` using `#isOperationAllowed(OnlineUser user, OperationType type, Position position)`
    * Use the `#isOperationAllowed` method that accepts and build an `Operation` via `Operation.builder()` for more complex operation checks!

<details>
<summary>Example &mdash; Checking what a user can do at a location</summary>

```java
void checkUserAccessAt(org.bukkit.Player player, org.bukkit.Location location) {
    OnlineUser user = huskTowns.getOnlineUser(player);
    Position position = huskTowns.getPosition(location);
    if (huskTowns.isOperationAllowed(user, OperationType.BREAK_BLOCKS, position)) {
        System.out.println("User can build here!");
    } else {
        System.out.println("User can't build here!");
    }
}
```
</details>

## 3. Creating a claim
* You can create a claim using `#createClaimAt(OnlineUser actor, Town town, Chunk chunk, World world)`
  * You may also create an admin claim using `#createAdminClaimAt(OnlineUser actor, Chunk chunk, World world)`
* This will create a claim at that position. You can then use `#getClaimAt(Position position)` to get the `TownClaim` object for the claim you just created (see #1)
* You can also create a claim at the chunk at a position using `#createClaimAt(OnlineUser actor, Town town, Position position)`

<details>
<summary>Example &mdash; Creating a claim</summary>

```java
void createClaimAt(org.bukkit.Player player, org.bukkit.Chunk chunk, org.bukkit.World world) {
    OnlineUser user = huskTowns.getOnlineUser(player);
    Town town = huskTowns.getTown("townName").get();
    huskTowns.createClaimAt(user, town, chunk, world);
}
```
</details>

### 3.1 Editing a claim
* You can edit a claim using `#editClaimAt(Chunk chunk, World world, Consumer<TownClaim> editor)`
* This will allow you to edit the claim at the given chunk and world using the `Consumer<TownClaim>` to modify the `TownClaim` object
  * For example, you can do `townClaim.claim().setType(Claim.Type type)` to change the type of the claim

<details>
<summary>Example &mdash; Editing a claim</summary>

```java
void editClaimAt(org.bukkit.Chunk chunk, org.bukkit.World world) {
    huskTowns.editClaimAt(chunk, world, townClaim -> {
        townClaim.claim().setType(Claim.Type.FARM);
    });
}
```
</details>

### 3.2 Deleting a claim
* You can delete a claim using `#deleteClaimAt(OnlineUser actor, Position position)`
  * A method that accepts a `Chunk` and a `World` is also available

<details>
<summary>Example &mdash; Deleting a claim</summary>

```java
void deleteClaimAt(org.bukkit.Player player, org.bukkit.Location location) {
    OnlineUser user = huskTowns.getOnlineUser(player);
    Position position = huskTowns.getPosition(location);
    huskTowns.deleteClaimAt(user, position);
}
```
</details>

### 4. Highlighting a claim
* You can "highlight" a claim for an `OnlineUser` (displaying the outline particle effect) using `#highlightClaim(OnlineUser actor, TownClaim claim)`
* You may additionally specify the duration, and use `#highlightClaimAt` to attempt to highlight a claim at a specified `Position`

<details>
<summary>Example &mdash; Highlighting a claim</summary>

```java
void highlightClaimAt(org.bukkit.Player player, org.bukkit.Location location) {
    OnlineUser user = huskTowns.getOnlineUser(player);
    Position position = huskTowns.getPosition(location);
    Optional<TownClaim> claim = huskTowns.getClaimAt(position);
    if (claim.isPresent()) {
        huskTowns.highlightClaim(user, claim.get());
    }
}
```
</details>