# CLAUDE.md for quarkus-one-time-starters

## Build & Test Commands

```bash
# Run full build (includes tests and static analysis)
./gradlew build

# Run tests only
./gradlew test

```

## Green Build Requirement

**CI must be green before merging a PR into `main`.** The `gradle.yml` workflow runs `./gradlew build` on every push, to any branch — there is no `branches: [main]` filter, so every commit is validated automatically.

- Run `./gradlew build` locally before pushing, to catch failures before they show up in Actions.
- If CI fails on a PR, fix the underlying issue — do not bypass it.
- Only use `[no ci]` in a commit message for changes that cannot affect the build (e.g. workflow or doc-only commits) — never to skip validation of code changes.

## Formatting

All code must follow the formatting rules in `.editorconfig`. The most important rules:

- **2-space indentation**, no tabs
- **CRLF line endings**
- **Max line length:** 180 characters
- **Insert final newline** in every file

Always format new and edited files according to `.editorconfig` before committing.

## Documentation

- **Architecture:** [docs/arc42.md](docs/arc42.md)

## Release Note Snippets

**Snippet filename:** `docs/releasenotes/snippets/{branch-last-segment}-{type}.md` where `{type}` is one of `bugfix` or `feature`.

**Snippet content:** Briefly describe what was changed or added on the branch. Each line should follow the pattern `* Description of the change(s).` Feel free to use multiple short lines, describing the change without technical detail. Only include **user-facing or dependency changes** in release notes. Do not add implementation details, refactoring notes, or internal structural changes (e.g. package renames, build task additions).

**Type selection:** Use `feature` for new user-facing functionality. Use `bugfix` for fixes and chore/internal changes (e.g. refactoring, configuration restructuring, dependency updates).
