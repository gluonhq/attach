# Gluon Attach #

Gluon [Attach](http://gluonhq.com/products/mobile/attach/) is the component that addresses the integration with low-level platform APIs in an end-to-end Java Mobile solution.

Attach provides an uniform, platform-independent API to access device and hardware features. 
At runtime, the appropriate implementation (attach:desktop, attach:android, attach:ios) makes sure the platform specific code is 
used to deliver the functionality.

Attach is open source, and it is freely licensed under the GPL license.
[Gluon](http://gluonhq.com) can provide [custom consultancy](http://gluonhq.com/services/consulting/) and [training](http://gluonhq.com/services/training/), commercial licenses, and open source [commercial support](http://gluonhq.com/services/commercial-support/), including daily and monthly releases.

## Getting started ##

The best way to get started with Gluon Attach is using the [Gluon plugin for your IDE](http://gluonhq.com/get-started/ide-plugins/)
and creating a [Gluon Mobile](http://gluonhq.com/products/mobile) project.

The [Gluon samples](http://gluonhq.com/developers/samples/) are a good way to find out how Attach is used.

See the [documentation](http://docs.gluonhq.com/charm/latest/#_charm_down) and the 
[Javadoc](http://docs.gluonhq.com/mobile/javadoc/latest/com/gluonhq/charm/down/package-summary.html).

The list of available services at Attach can be found [here](http://gluonhq.com/products/mobile/attach/).


## Issues and Contributions ##

Issues can be reported to the [Issue tracker](https://github.com/gluonhq/attach/issues)

Contributions can be submitted via [Pull requests](https://github.com/gluonhq/attach/pulls)


## Building Attach ##

Gluon Attach is frequently released, and this is only required in case you want to fork and build your local version of Attach.

### Requisites ###

These are the requisites:

* A recent version of [JDK 11](http://jdk.java.net/11/)
* Gradle 5.0 or superior. 

To build the iOS Services:
 
* A Mac with with MacOS X 10.11.5 or superior
* XCode 9.x or superior

### How to build and install Attach ###

To build the Attach services on the project's root, run:

`./gradlew clean build`

If you want to install them, run:

`./gradlew  clean publishToMavenLocal`

To build/install for desktop, add the option `-Pdesktop`. 

To build/install for iOS, add the option `-Pios`.

When the process finishes successfully, the different services can be added to a Gluon Mobile project 
by including `mavenLocal()` in the list of repositories.

For instance, the Lifecycle service for desktop can be added to the project like:

```
repositories {
    mavenLocal()
}

dependencies {
    implementation 'com.gluonhq.attach:lifecycle:4.0.0-SNAPSHOT:desktop'
}
```