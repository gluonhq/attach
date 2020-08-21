# Gluon Attach #

Gluon [Attach](http://gluonhq.com/products/mobile/attach/) is the component that addresses the integration with low-level platform APIs in an end-to-end Java Mobile solution.

Attach provides an uniform, platform-independent API to access device and hardware features. 
At runtime, the appropriate implementation (attach:desktop, attach:android, attach:ios) makes sure the platform specific code is 
used to deliver the functionality.

Attach is open source, and it is freely licensed under the GPL license.
[Gluon](http://gluonhq.com) can provide [custom consultancy](http://gluonhq.com/services/consulting/) and [training](http://gluonhq.com/services/training/), commercial licenses, and open source [commercial support](http://gluonhq.com/services/commercial-support/), including daily and monthly releases.

[![Maven Central](https://img.shields.io/maven-central/v/com.gluonhq.attach/util)](https://search.maven.org/search?q=g:com.gluonhq.attach%20AND%20a:util)
[![Travis CI](https://api.travis-ci.com/gluonhq/attach.svg?branch=master)](https://travis-ci.com/gluonhq/attach)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)


## Getting started ##

The best way to get started with Gluon Attach is using the [Gluon Client Maven archetype from your IDE](https://github.com/gluonhq/client-maven-archetypes)
and creating a [Gluon Mobile](http://gluonhq.com/products/mobile) project.

The [Gluon Client samples](https://github.com/gluonhq/client-samples) are a good way to find out how Attach is used.

See the [documentation](https://docs.gluonhq.com/client/#_attach_configuration) on how to add Attach with the Client Plugin.

The list of available services at Attach can be found [here](http://gluonhq.com/products/mobile/attach/).


## Issues and Contributions ##

Issues can be reported to the [Issue tracker](https://github.com/gluonhq/attach/issues)

Contributions can be submitted via [Pull requests](https://github.com/gluonhq/attach/pulls), 
 providing you have signed the [Gluon Individual Contributor License Agreement (CLA)](https://docs.google.com/forms/d/16aoFTmzs8lZTfiyrEm8YgMqMYaGQl0J8wA0VJE2LCCY) 
 (See [What is a CLA and why do I care](https://www.clahub.com/pages/why_cla) in case of doubt).


## Building Attach ##

Gluon Attach is frequently released, and this is only required in case you want to fork and build your local version of Attach.

### Requisites ###

These are the requisites:

* A recent version of [JDK 11](http://jdk.java.net/11/)
* Gradle 6.0 or superior. 

To build the iOS Services:
 
* A Mac with with MacOS X 10.14.4 or superior
* XCode 11.x or superior

### How to build and install Attach ###

To build the Attach services on the project's root, run:

`./gradlew clean build`

If you want to install them, run:

`./gradlew clean publishToMavenLocal`

When the process finishes successfully, the different services can be added to a Gluon Mobile project.

For instance, the Display service for desktop can be added to the project like:

```
<!-- dependencies -->
<dependency>
    <groupId>com.gluonhq.attach</groupId>
    <artifactId>display</artifactId>
    <version>4.0.9-SNAPSHOT</version>
</dependency>

<!-- plugin -->
<configuration>
    <attachList>
        <list>display</list>
    </attachList>
</configuration>
```
