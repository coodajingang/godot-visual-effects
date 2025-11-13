# Changelog

All notable changes to this project will be documented in this file.

The format is inspired by [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) and the project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2024-11-13
### Added
- Initial Godot 4 project scaffold (`/godot4-universal-framework`) with modular architecture.
- Event-driven framework core (`GameApp`) and reusable addons (EventBus, AuthKit, DataKit, PayKit, SocialKit, NetKit, LogKit, GameKit).
- Supabase and Firebase backend adapters plus a mock backend service for offline demos.
- Demo UI (`Main.tscn`) showcasing login → profile loading → cloud sync → store purchase flow.
- Developer tooling: debug console singleton, localization manager, minimal environment configuration.
- Comprehensive documentation set: architecture overview, module deep dives, backend setup guide, demo instructions.

### Planned
- Concrete Supabase REST service implementation and Firebase REST/WebSocket bindings.
- Automated integration tests for key gameplay flows and backend adapters.
- Editor UI widgets for inspecting EventBus traffic and runtime module states.
