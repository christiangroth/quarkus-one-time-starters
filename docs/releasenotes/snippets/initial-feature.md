* fix-initial-build: Introduced `Starter` API interface for implementing one-time startup tasks.
* fix-initial-build: Added `StarterStatus` and `StarterCompletionFlag` to track execution state of each starter.
* fix-initial-build: Implemented `StarterService` to run all registered starters in alphabetical order on application startup, persisting results to MongoDB.
* fix-initial-build: Added `StarterStartup` that observes the Quarkus `StartupEvent` and triggers all starters in production mode only.
* fix-initial-build: Introduced `StarterSkipPredicate` to block the Quarkus scheduler until all starters have completed successfully.
* fix-initial-build: Metrics support via Micrometer — exposes per-starter execution duration and overall status as Gauges.
