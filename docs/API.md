![HuskTowns CI](https://jitpack.io/v/WiIIiam278/HuskTowns.svg)

The HuskTowns API provides methods for interfacing and editing towns, claims and users, alongside a selection of API events for listening to when players perform certain town actions.

Note that the HuskTowns API v2 is different from the deprecated, [legacy HuskTowns API v1](API-v1.md).

The API is distributed via [JitPack](https://jitpack.io/#net.william278/HuskTowns).
(Some) javadocs are also available to view on JitPack [here](https://javadoc.jitpack.io/net/william278/HuskTowns/latest/javadoc/).

## Table of contents
1. Adding the API to your project
2. Adding HuskTowns as a dependency
3. Next steps

## API Introduction
### 1.1 Setup with Maven
<details>
<summary>Maven setup information</summary>

Add the repository to your `pom.xml` as per below.
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
Add the dependency to your `pom.xml` as per below. Replace `version` with the latest version of HuskTowns (without the v): ![Latest version](https://img.shields.io/github/v/tag/WiIIiam278/HuskTowns?color=%23282828&label=%20&style=flat-square)
```xml
<dependency>
    <groupId>net.william278</groupId>
    <artifactId>HuskTowns</artifactId>
    <version>version</version>
    <scope>provided</scope>
</dependency>
```
</details>

### 1.2 Setup with Gradle
<details>
<summary>Gradle setup information</summary>

Add the dependency like so to your `build.gradle`:
```groovy
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}
```
Add the dependency as per below. Replace `version` with the latest version of HuskTowns (without the v): ![Latest version](https://img.shields.io/github/v/tag/WiIIiam278/HuskTowns?color=%23282828&label=%20&style=flat-square)

```groovy
dependencies {
    compileOnly 'net.william278:HuskTowns:version'
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