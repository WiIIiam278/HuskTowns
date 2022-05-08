[![Header](https://i.imgur.com/JckKnZZ.png "Header")](https://github.com/WiIIiam278/HuskTownsAPI/)
# HuskTownsAPI
[![Discord](https://img.shields.io/discord/818135932103557162?color=7289da&logo=discord)](https://discord.gg/tVYhJfyDWG)

This repository contains the API module of [HuskTowns](https://github.com/WiIIiam278/HuskTowns). You can find out more about the HuskTowns API on the [wiki page](https://github.com/WiIIiam278/HuskTowns/wiki/API).

## Links
* Documentation for the API, including a list of methods and example usages [can be found here](https://github.com/WiIIiam278/HuskTowns/wiki/API).
* Browse the [HuskTownsAPI JavaDocs here](https://javadoc.jitpack.io/com/github/WiIIiam278/HuskTowns/latest/javadoc/).

## Getting the API
HuskTownsAPI is available [from JitPack](https://jitpack.io/#WiIIiam278/HuskTowns).

### Gradle
Add the JitPack repository in your root build.gradle at the end of repositories:
```groovy
	allprojects {
		repositories {
			maven { url 'https://jitpack.io' }
		}
	}
```
Add the dependency, replacing Tag with the latest version of HuskTowns
```groovy
	dependencies {
	        implementation 'com.github.WiIIiam278:HuskTowns:Tag'
	}
```

### Maven
Add the JitPack repository to your pom.xml
```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```
Add the dependency, replacing Tag with the latest version of HuskTowns
```xml
	<dependency>
	    <groupId>com.github.WiIIiam278</groupId>
	    <artifactId>HuskTowns</artifactId>
	    <version>Tag</version>
	</dependency>
```