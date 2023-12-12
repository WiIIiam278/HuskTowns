The HuskTowns API provides methods for interfacing and editing towns, claims and users, alongside a selection of API events for listening to when players perform certain town actions.

## Compatibility
[![Maven](https://repo.william278.net/api/badge/latest/releases/net/william278/husktowns?color=00fb9a&name=Maven&prefix=v)](https://repo.william278.net/#/releases/net/william278/husktowns/)

The HuskTowns API shares version numbering with the plugin itself for consistency and convenience. Please note minor and patch plugin releases may make API additions and deprecations, but will not introduce breaking changes without notice.

| API Version |  HuskTowns Versions  | Supported |
|:-----------:|:--------------------:|:---------:|
|    v2.x     | _v2.0&mdash;Current_ |     ✅     |
|    v1.x     | _v1.0&mdash;Current_ |     ⚠️     |

> **Warning:** The HuskTowns API v1 is deprecated. [Click here for API v1 Docs&hellip;](API-v1)

<details>
<summary>Targeting older versions</summary>

HuskTowns versions prior to `v2.3.1` are distributed on [JitPack](https://jitpack.io/#/net/william278/HuskTowns), and you will need to use the `https://jitpack.io` repository instead.
</details>

## Table of contents
1. Adding the API to your project
2. Adding HuskTowns as a dependency
3. Next steps

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
    <groupId>net.william278</groupId>
    <artifactId>husktowns</artifactId>
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
    compileOnly 'net.william278:husktowns:VERSION'
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

### 3. Next steps
Now that you've got everything ready, you can start doing stuff with the HuskTowns API!
- [[API Examples]]
- [[API Events]]
