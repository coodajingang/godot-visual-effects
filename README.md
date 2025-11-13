# Godot4 Universal Game Framework (Supabase & Firebase Ready)

The **Godot4 Universal Game Framework** is a modular starter kit that helps small and mid-sized teams ship connected games faster. It provides pluggable gameplay services (auth, data, payments, social, analytics, live ops) alongside adapters for **Supabase** and **Firebase**, plus a fully wired demo scene that walks through the core player journey: **login → load profile → sync cloud save → open the store → complete a purchase**.

> ✨ This repository contains both the framework code (`/godot4-universal-framework`) and the accompanying documentation (`/docs/framework`).

---

## Highlights

- **Message bus architecture** – decoupled modules communicate through an in-process event bus.
- **Drop-in kits** – AuthKit, DataKit, PayKit, SocialKit, NetKit, LogKit, and GameKit are self-contained and can be enabled individually.
- **Backend abstraction** – switch between Supabase and Firebase adapters (or mock mode) with configuration only.
- **Mock-friendly demo** – runnable login & store showcase that works without external services.
- **Developer tooling** – in-engine debug console, analytics stubs, localization hooks, runtime configuration entry points.
- **Extensive docs** – architecture, module APIs, extension guides, and backend setup instructions.

---

## Repository layout

```
/
├─ godot4-universal-framework/      # Godot 4 project containing the framework & demo
│  ├─ addons/universal_framework/    # Modular kits and shared services (event bus, adapters, devtools)
│  ├─ framework/                     # Runtime bootstrap (GameApp autoload)
│  ├─ frontend/                      # Demo UI scripts
│  ├─ scenes/                        # Main.tscn demo entry point
│  └─ project.godot                  # Godot project configuration
├─ docs/framework/                   # Design documents & module guides
│  ├─ architecture.md
│  └─ modules/*.md
├─ CHANGELOG.md                      # Release history & roadmap
├─ SETUP.md                          # Supabase / Firebase provisioning guide
└─ demo_run_instructions.md          # Step-by-step demo execution instructions
```

---

## Quick start

1. Install **Godot Engine 4.2+**.
2. Clone this repository and open the project located at `/godot4-universal-framework` from the Godot project manager.
3. Run the `Main.tscn` scene. You will be greeted with the login screen used in the tutorial workflow.
4. Follow the [demo instructions](./demo_run_instructions.md) for the guided flow and available debug shortcuts.

### Demo flow

1. Enter an email (or press **Guest Login**).
2. The mock backend returns a profile and the DataKit synchronizes cloud save data.
3. Press **Open Store** to fetch the storefront and list mock products.
4. Purchase an item to trigger currency rewards, inventory updates, analytics logging, and UI feedback.
5. Press **Sync Cloud Save** at any time to run a round-trip save with the mock backend.

---

## Framework modules at a glance

| Layer | Module | Purpose |
|-------|--------|---------|
| Game Framework | **AuthKit** | Authentication entry points (guest, email/password, OAuth), identity verification hooks |
| Game Framework | **DataKit** | Local cache management, cloud save reconciliation, persistence helpers |
| Game Framework | **PayKit** | Storefront loading, virtual currency, purchase pipeline, reward delivery |
| Game Framework | **SocialKit** | Friends, chat, tasks/achievements, leaderboard aggregation |
| Game Framework | **NetKit** | Unified HTTP/WebSocket abstraction, polling utilities |
| Game Framework | **LogKit** | Analytics & exception reporting (Firebase Analytics ready) |
| Game Framework | **GameKit** | Core gameplay helpers (battle settlement, stages, buff formulas, scheduler) |
| Backend Integration | **SupabaseAdapter / FirebaseAdapter** | Provider-specific API bridges implementing the shared interface |
| Shared | **EventBus, LocalizationManager, DebugConsole** | Cross-module infrastructure living under `addons/` |

Every module is documented in detail under [`docs/framework/modules`](./docs/framework/modules/).

---

## Backend setup

The project ships with a mock backend for offline prototyping. When you are ready to connect to real services:

1. Follow the provisioning steps in [`SETUP.md`](./SETUP.md) to create Supabase and/or Firebase projects.
2. Update the configuration block in `framework/core/game_app.gd` to point to your credentials or inject them at runtime.
3. Replace the mock service wiring with the concrete Supabase / Firebase service calls (reference the adapter contracts in `docs/framework/modules/backend_adapters.md`).

---

## Documentation & further reading

- [`docs/framework/architecture.md`](./docs/framework/architecture.md) – high-level system design, module dependencies, and data flows.
- [`docs/framework/modules`](./docs/framework/modules) – deep dives for each kit and backend adapter.
- [`SETUP.md`](./SETUP.md) – cloud project initialization and configuration hints.
- [`CHANGELOG.md`](./CHANGELOG.md) – release notes, roadmap, and planned enhancements.

---

## Contributing & feedback

Contributions are welcome! Please open an issue or submit a pull request describing your enhancement. When contributing code, keep modules isolated and communicate through the event bus to maintain the plug-and-play architecture.

---

## License

All code in this repository is released under the MIT License. Art assets (if any) are licensed as CC-BY 4.0. See [LICENSE](./LICENSE) for details.
