HuskTowns exposes API to let you register `OperationType`s to call custom operations and check if they should be allowed with the `Handler`.

## Table of Contents
* [1. Getting the OperationTypeRegistry](#1-getting-the-operationtyperegistry)
* [2. Operations and OperationTypes](#2-operations-and-operationtypes)
* [3. Registering a custom OperationType](#3-registering-a-custom-operationtype)
* [4. Using Handler to cancel Operations with your custom OperationType](#4-using-handler-to-cancel-operations-with-your-custom-operationtype)

## 1. Getting the OperationTypeRegistry
* The `OperationTypeRegistry` is the registry containing registered `OperationType`s.
* It also lets you access the `Handler` class.
* Get the `OperationTypeRegistry` with `HuskTownsAPI#getOperationTypeRegistry`

<details>
<summary>Example &mdash; Getting the OperationTypeRegistry</summary>

```java
void getRegistry() {
    final OperationTypeRegistry reg = HuskTowns.getOperationTypeRegistry();
}
```
</details>

## 2. Operations and OperationTypes
* `Operation`s represent actions in a world that can be prevented from occuring entity based on:
  * Which game entity, if any, performed the action
  * If the game entity is a player, their trust level (see [[Trust API]])
  * The location the action affects or occurred
  * The game entity, if any, affected by the action
* The type of action that occurred is represented by an `OperationType`.
* Your plugin can create `Operation`s with custom registered `OperationType`s and call `Handler#isOperationAllowed` to allow to determine whether your custom plugin/mod's actions should be permitted 
* Users can then add these `OperationType`s to their [trust levels](Trust) config.

## 3. Registering a custom OperationType
* Start by registering a custom `OperationType`. We recommend doing this `onEnable` (not `onLoad` before HuskTowns has loaded).
* Use `OperationTypeRegistry#registerOperationType(@NotNull Key key, boolean silent)` to create an `OperationType`
  * `key` - the key identifier of the OperationType (e.g. `Key.of("my_plugin", "operation_name")` -> `my_plugin:operation_name`).
  * `silent` - whether players should be informed when this operation is cancelled (usually you want this on, unless your operation doesn't affect players or is particularly spammy such as pressure plate triggering operations)
* This method will create, then register an `OperationType`. You should save this Operation Type somewhere for later use.

<details>
<summary>Example &mdash; Registering a custom OperationType</summary>

```java
private OperationType releaseMonOpType;

void getRegistry() {
    final OperationTypeRegistry reg = HuskTowns.getOperationTypeRegistry();
    releaseMonOpType = reg.createOperationType(Key.of("mons_mod", "release_mon"));
    reg.registerOperationType(releaseMonOpType);
}
```
</details>

## 4. Using Handler to cancel Operations with your custom OperationType
* Get the `Handler` with `OperationTypeRegistry#getHandler`
* Create an `Operation` with your newly made `OperationType` using `Operation#of()`
  * There are different static `Operation#of` variants letting you specify the performing user, operation type, operation position, silent state of this operation (overriding the default value of the operation type), and victim user in various combinations.
* Call `Handler#cancelOperation` to get a boolean `true` or `false` value of whether the Operation should be allowed
* Decide whether to perform logic based on that return value

<details>
<summary>Example &mdash; Checking if Operations are allowed</summary>

```java
private OperationType releaseMonOpType;

void onMonReleased(Player bukkitPlayer, Location releasedAt) {
  final OperationTypeRegistry reg = HuskTowns.getOperationTypeRegistry();
  final boolean cancelled = reg.getHandler().cancelOperation(Operation.of(
        HuskTowns.getPlayer(bukkitPlayer), // OnlineUser implements OperationUser
        releaseMonOpType,
        HuskTowns.getPosition(releasedAt) // Position implements OperationPosition
    ));
    if (cancelled) {
        // Don't continue with the action
        return;
    }
    // Logic would continue if the operation wasn't cancelled...
}
```
</details>