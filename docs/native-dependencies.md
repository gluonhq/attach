# Native dependency manifests

Some Attach services need extra native dependencies that are not part of Attach
itself (e.g. a third-party SDK such as Google Mobile Ads). Rather than vendoring
those SDKs, a service declares them through a small manifest file under
`src/main/resources/META-INF/substrate/`. At build time, [Substrate](https://github.com/gluonhq/substrate)
reads this manifest from the service's jar and resolves/downloads/links the
declared dependencies when it builds the final application.

There are two manifests, one per platform. Both live under
`META-INF/substrate/<platform>/`, are plain text, and ignore blank lines and
lines starting with `#`.

## Android: `META-INF/substrate/dalvik/android-dependencies.txt`

One Gradle dependency-notation line per entry. Each line is inserted verbatim
into the generated Android project's `dependencies {}` block, replacing the
`// OTHER_ANDROID_DEPENDENCIES` marker in Substrate's
[`build.gradle` template](https://github.com/gluonhq/substrate/blob/master/src/main/resources/native/android/android_project/app/build.gradle).

### Example

```
implementation 'com.android.support:appcompat-v7:28.0.0'
implementation 'com.google.android.gms:play-services-ads:24.4.0'
```

## iOS: `META-INF/substrate/ios/ios-frameworks.txt`

iOS third-party dependencies are usually shipped as `.xcframework` bundles
rather than a dependency-manager coordinate, so this manifest is a bit richer.
It is parsed by Substrate's
[`Frameworks`](https://github.com/gluonhq/substrate/blob/master/src/main/java/com/gluonhq/substrate/util/ios/Frameworks.java)
class, which is the authoritative reference for the grammar below — keep this
document (and the Groovy-side reader in Attach's own
[`native-build.gradle`](../gradle/native-build.gradle), used only to resolve
compile-time framework headers while building `lib<Service>.a`) in sync with it
whenever the grammar changes.

One entry per line, `<keyword> <value>`:

| Keyword | Value | Effect                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|---|---|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `sdk-zip` | a URL | Downloads this zip on demand and caches it under `~/.gluon/substrate/ios-sdk`, then searches it (recursively) for the `embed` xcframeworks declared below. The SDK is never vendored in the repository.                                                                                                                                                                                                                                                                                             |
| `embed` | `<Name>.xcframework` | Makes this framework's slice available to the linker (`-F <slice>` + `-Wl,-framework,<Name>`). If its binary is a *static* archive (as e.g. the Google Mobile Ads SDK ships it), its code is linked directly into the app and it is **not** copied into `<App>.app/Frameworks`. If it is *dynamic*, it is copied there, code-signed, and reached at runtime through an `@executable_path/Frameworks` rpath. Static Swift frameworks additionally pull in the Swift runtime/compatibility libraries. |
| `framework` | `<Name>` | Adds `-Wl,-framework,<Name>` (a system framework not already in Substrate's default iOS framework list). The list can be generated from the `<Name>.xcframework/ios-arm64/<frameworkName>.framework/Modules/module.modulemap` file, finding lines like `link framework "AdSupport"`.                                                                                                                                                                                                                  |
| `weak-framework` | `<Name>` | Adds `-Wl,-weak_framework,<Name>`, for a framework that may not be available on all supported iOS versions/SDKs.                                                                                                                                                                                                                                                                                                                                                                                    |
| `library` | `<name>` | Adds `-l<name>` (a system dylib, e.g. `z`, `sqlite3`). The list can be generated from the `<Name>.xcframework/ios-arm64/<frameworkName>.framework/Modules/module.modulemap` file, finding lines like `link z`.                                                                                                                                                                                                                                                                                        |

Substrate's built-in iOS framework list already covers `Foundation`, `UIKit`, `CoreGraphics`, `CoreMedia`, `CoreMotion`, `CoreLocation`, `AVFoundation`,
`AudioToolbox`, `StoreKit`, `WebKit`, `UserNotifications`,`AuthenticationServices` frameworks. So only declare `framework`/`weak-framework` entries here for anything beyond that list.

Note that in Attach, the static frameworks won't be included with the native library built for the service. They will be embedded into the final native image, along with the libraries, via Substrate.

### Example

```
# Pinned, reproducible download of the SDK zip:
sdk-zip https://dl.google.com/googleadmobadssdk/googlemobileadssdkios-13.5.0.zip

# Static xcframeworks shipped inside the zip above:
embed GoogleMobileAds.xcframework
embed UserMessagingPlatform.xcframework

# Extra system frameworks required by GoogleMobileAds.framework's module map:
framework AdSupport
framework CFNetwork
framework CoreTelephony
framework JavaScriptCore
framework MessageUI
framework Network
framework SafariServices
framework Security
framework SystemConfiguration
# Extra system frameworks required:
framework AppTrackingTransparency

# Only available on iOS 17.4+ SDKs, so declared weak:
weak-framework MarketplaceKit

# System libraries linked by GoogleMobileAds.framework:
library z
library sqlite3
```

