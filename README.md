# AssertJ M2E Connector

A plugin for Eclipse (m2e) to support AssertJ assertion generation in Maven projects.

The current version 0.4.0 supports m2e 1.8 and thus eclipse Neon.

## Features

**Source folders:** Automatically configure the source folders for the generated assertion classes.

**Refresh:** Automatically refresh the generated assertion classes upon source changes.

## Usage

The plugin starts in the "Maven->Update Project Configuration..." cycle. (Rightclick on the Project->Maven->Update Project Configuration...).

The plugin is activated for any Maven Project that uses assertj-assertions-generator-maven-plugin:generate-assertions.

IMPORTANT: The Plugin onlys works, when you don't ignore the M2E-Connector for assertj-assertions-generator-maven-plugin. When you have added an ignore to your POMs or the eclipse preferemces, you need to remove them.

## Installation

### Eclipse-Marketplace:

https://marketplace.eclipse.org/content/assertj-m2e-connector

### Update site:

https://github.com/hennejg/m2e-assertj/raw/master/updateSite/

## Changes

* 0.1.0: initial version
* 0.2.0: compatibility with eclipse Neon
* 0.3.0: trigger refresh on pom change
* 0.4.0: use target/test-classes instead of target/classes as output location for generated sources
