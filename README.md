# AssertJ M2E Connector

Plugin for  Eclipse (m2e) to support AssertJ assertion generation in Maven projects

The current version 1.0.0.5 supports m2e 1.7 and thus eclipse Mars.

## Features

**Refresh:** Automatically refresh the generated assertion classes upon source change.

## Usage

The plugin starts in the "Maven->Update Project Configuration..." cycle. (Rightclick on the Project->Maven->Update Project Configuration...).

The plugin is activated for any Maven Project that uses assertj-assertions-generator-maven-plugin:generate-assertions.

IMPORTANT: The Plugin onlys works, when you don't ignore the M2E-Connector for assertj-assertions-generator-maven-plugin. When you have added this ignore to your pom.xml's, you need to remove that.

## Installation

### Eclipse-Marketplace:

WORK IN PROGRESS, does not yet work.
http://marketplace.eclipse.org/content/assertj-m2e-connector

### Update site:

https://github.com/hennejg/assertj-m2e/raw/master/updateSite/

## Changes

* 0.1.0: initial version
