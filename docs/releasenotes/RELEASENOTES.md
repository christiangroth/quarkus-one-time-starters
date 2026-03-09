# 0.4.0 (2026.03.09)

## New Features
* add-sources-publishing: Sources JARs are now published alongside the compiled JARs to GitHub Packages.



---
# 0.3.1 (2026.03.09)

## Bugfixes / Chore
* remove-kotlin-bom: Removed explicit Kotlin BOM dependency from convention plugin, as it is already managed by the Quarkus BOM, preventing duplicate entries in generated POMs.



---
# 0.3.0 (2026.03.08)

## New Features
* review-and-cleanup: Added `README.md` with usage instructions and getting started guide.
* review-and-cleanup: Added compact arc42 architecture documentation in `docs/arc42.md` with PlantUML diagrams via Kroki.
* review-and-cleanup: Added missing tests for `StarterCompletionFlag` and `StarterStartup`, and extended `StarterService` tests to cover Micrometer metrics.



---
# 0.2.0 (2026.03.08)

## New Features
* split-up-impl-module: Renamed `api` module to `domain-api` and split `impl` into `domain-impl` and `adapter-out-persistence-mongodb`.
* split-up-impl-module: MongoDB collection indexes are now synced on startup — obsolete indexes are removed and predefined ones are added.



---
# 0.1.0 (2026.03.08)

## New Features
* fix-initial-build: Introduced `Starter` API interface for implementing one-time startup tasks.
* fix-initial-build: Added `StarterStatus` and `StarterCompletionFlag` to track execution state of each starter.
* fix-initial-build: Implemented `StarterService` to run all registered starters in alphabetical order on application startup, persisting results to MongoDB.
* fix-initial-build: Added `StarterStartup` that observes the Quarkus `StartupEvent` and triggers all starters in production mode only.
* fix-initial-build: Introduced `StarterSkipPredicate` to block the Quarkus scheduler until all starters have completed successfully.
* fix-initial-build: Metrics support via Micrometer — exposes per-starter execution duration and overall status as Gauges.

## Bugfixes / Chore
* fix-publishing-dependency-issue: Fixed publishing validation error for Maven artifacts.



---
