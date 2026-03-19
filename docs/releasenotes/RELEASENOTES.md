# 0.6.1 (2026.03.19)

## Bugfixes / Chore
* `StartupPort` moved to internal domain-impl; replaced by `StartersStatusProvider` with `allCompleted()` in the public API.



---
# 0.6.0 (2026.03.16)

## New Features
* `domain-api` is now the minimal client-facing artifact, containing only the `Starter` interface.
* Port contracts and `ScheduledSkipPredicate` have moved to `domain-impl`; clients no longer see internal port interfaces.
* Clients implementing a custom persistence adapter now depend on `domain-impl`.



---
# 0.5.0 (2026.03.16)

## New Features
* Refactored code structures to better fit hexagonal architecture
* Added KDoc documentation to all domain-api interfaces and classes
* Updated arc42 architecture documentation to reflect current class and interface names.



---
# 0.4.0 (2026.03.09)

## New Features
* Sources JARs are now published alongside the compiled JARs to GitHub Packages.



---
# 0.3.1 (2026.03.09)

## Bugfixes / Chore
* Removed explicit Kotlin BOM dependency from convention plugin, as it is already managed by the Quarkus BOM, preventing duplicate entries in generated POMs.



---
# 0.3.0 (2026.03.08)

## New Features
* Added `README.md` with usage instructions and getting started guide.
* Added compact arc42 architecture documentation in `docs/arc42.md` with PlantUML diagrams via Kroki.
* Added missing tests for `StarterCompletionFlag` and `StarterStartup`, and extended `StarterService` tests to cover Micrometer metrics.



---
# 0.2.0 (2026.03.08)

## New Features
* Renamed `api` module to `domain-api` and split `impl` into `domain-impl` and `adapter-out-persistence-mongodb`.
* MongoDB collection indexes are now synced on startup — obsolete indexes are removed and predefined ones are added.



---
# 0.1.0 (2026.03.08)

## New Features
* Introduced `Starter` API interface for implementing one-time startup tasks.
* Added `StarterStatus` and `StarterCompletionFlag` to track execution state of each starter.
* Implemented `StarterService` to run all registered starters in alphabetical order on application startup, persisting results to MongoDB.
* Added `StarterStartup` that observes the Quarkus `StartupEvent` and triggers all starters in production mode only.
* Introduced `StarterSkipPredicate` to block the Quarkus scheduler until all starters have completed successfully.
* Metrics support via Micrometer — exposes per-starter execution duration and overall status as Gauges.

## Bugfixes / Chore
* Fixed publishing validation error for Maven artifacts.



---
