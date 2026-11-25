# Architecture

`BungeeBridge` is the only platform adapter. It reads game state through BungeeCord, schedules main-platform operations,
answers proxy ping events and delegates protocol behavior to the vendored Core. Core owns pairing, HTTP, configuration,
telemetry shaping, MOTD proof state, Floodgate lookup, rewards, retry ordering and credential handling.

The adapter sends Core an aggregate `Stats` snapshot. Player UUIDs are used only in memory to ask Floodgate which connected
players are Bedrock users; telemetry construction discards identifiers. Backend names can be included when enabled, but
backend addresses are never collected.

`core.lock` binds the vendored `core/src` tree to an immutable Core tag, commit and source hash. Both Gradle and CI reject a
changed tree. This keeps the three platform implementations on one protocol implementation without a runtime repository.
