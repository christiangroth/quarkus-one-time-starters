* adapt-project-structure: `domain-api` is now the minimal client-facing artifact, containing only the `Starter` interface.
* adapt-project-structure: Port contracts and `ScheduledSkipPredicate` have moved to `domain-impl`; clients no longer see internal port interfaces.
* adapt-project-structure: Clients implementing a custom persistence adapter now depend on `domain-impl`.
