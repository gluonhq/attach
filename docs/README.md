# Attach developer documentation

This folder collects developer-facing documentation for people contributing new
Attach services, or changes to existing ones. It complements the top-level
[README](../README.md), which is aimed at consumers of Attach.

* [Native dependency manifests](native-dependencies.md) — how a service declares extra
  Android (`android-dependencies.txt`) or iOS (`ios-frameworks.txt`) native dependencies
  that Substrate must pull in when building a downstream application.

More documents will be added here over time (module layout, adding a new service,
testing, release process, etc). If something is missing or unclear, please open an
issue or a pull request.
